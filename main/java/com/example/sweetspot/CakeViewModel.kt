package com.example.sweetspot

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sweetspot.models.Cake
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class CakeViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _cakeState = MutableLiveData<CakeState>()
    val cakeState: LiveData<CakeState> = _cakeState

    private val _cakeList = MutableLiveData<List<Cake>>()
    val cakeList: LiveData<List<Cake>> = _cakeList

    private val favoritesMap = mutableStateMapOf<String, Boolean>()

    fun fetchCakes() {
        db.collection("cakes")
            .whereEqualTo("isDeleted", false)
            .get()
            .addOnSuccessListener { result ->
                val cakes = result.documents.mapNotNull { doc ->
                    doc.toObject(Cake::class.java)?.copy(id = doc.id)
                }
                _cakeList.value = cakes
            }
            .addOnFailureListener { e ->
                _cakeList.value = emptyList()
                Log.e("CakeViewModel", "Error fetching cakes", e)
            }
    }

    suspend fun fetchFavoriteCakes(userId: String): List<String> {
        return try {
            val snapshot = db.collection("favorites")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("cakeId") }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCakeById(cakeId: String): Flow<Cake?> = flow {
        val cakeSnapshot = db.collection("cakes")
            .document(cakeId)
            .get()
            .await()

        val cake = cakeSnapshot.toObject(Cake::class.java)?.copy(id = cakeSnapshot.id)
        emit(cake)
    }

    fun getCakeByName(name: String): Flow<Cake?> = flow {
        val cakeSnapshot = db.collection("cakes")
            .whereEqualTo("name", name)
            .limit(1)
            .get()
            .await()

        val cake = cakeSnapshot.documents.firstOrNull()?.toObject(Cake::class.java)
        emit(cake)
    }

    fun addCake(
        name: String,
        description: String,
        priceSmall: Double,
        priceMedium: Double,
        priceLarge: Double,
        category: String,
        imageUri: Uri?,
        imageName: String
    ) {
        if (name.isBlank() || description.isBlank() || category.isBlank() || priceSmall <= 0.0 || priceMedium <= 0.0 || priceLarge <= 0.0) {
            _cakeState.value = CakeState.Error("Fields cannot be empty and prices must be greater than 0")
            return
        }

        _cakeState.value = CakeState.Loading

        imageUri?.let {
            uploadImageToStorage(it, imageName) { imageUrl ->
                if (imageUrl != null) {
                    saveCakeToFirestore(name, description, priceSmall, priceMedium, priceLarge, category, imageUrl, 0)  // Initialize orders as 0
                } else {
                    _cakeState.value = CakeState.Error("Image upload failed")
                }
            }
        } ?: run {
            saveCakeToFirestore(name, description, priceSmall, priceMedium, priceLarge, category, "", 0)  // Initialize orders as 0
        }
    }

    fun updateCake(
        cakeId: String,
        name: String,
        description: String,
        priceSmall: Double,
        priceMedium: Double,
        priceLarge: Double,
        category: String,
        newImageUri: Uri?
    ) {
        _cakeState.value = CakeState.Loading

        if (newImageUri != null) {
            val imageName = "$name.png"
            uploadImageToStorage(newImageUri, imageName) { imageUrl ->
                if (imageUrl != null) {
                    updateCakeInFirestore(cakeId, name, description, priceSmall, priceMedium, priceLarge, category, imageUrl)
                } else {
                    _cakeState.value = CakeState.Error("Image update failed")
                }
            }
        } else {
            val currentImageUrl = cakeList.value?.find { it.id == cakeId }?.imageUrl.orEmpty()
            updateCakeInFirestore(cakeId, name, description, priceSmall, priceMedium, priceLarge, category, currentImageUrl)
        }
    }

    fun incrementOrders(cakeId: String) {
        db.collection("cakes").document(cakeId)
            .update("orders", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("CakeViewModel", "Order count incremented successfully")
            }
            .addOnFailureListener { e ->
                Log.e("CakeViewModel", "Failed to increment orders", e)
            }
    }

    fun softDeleteCake(cakeId: String) {
        db.collection("cakes").document(cakeId)
            .update("isDeleted", true)
            .addOnSuccessListener {
                _cakeState.value = CakeState.Success("Cake soft-deleted successfully")
                fetchCakes()  // Refresh the cake list after soft deletion
            }
            .addOnFailureListener { e ->
                _cakeState.value = CakeState.Error(e.message ?: "Failed to soft delete cake")
            }
    }

    fun resetState() {
        _cakeState.value = CakeState.Idle
    }

    // Upload the image to Firebase Storage
    private fun uploadImageToStorage(imageUri: Uri, imageName: String, onResult: (String?) -> Unit) {
        val fileName = "cakes/$imageName"
        val storageRef = storage.reference.child(fileName)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onResult(uri.toString())
                }.addOnFailureListener {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun saveCakeToFirestore(
        name: String,
        description: String,
        priceSmall: Double,
        priceMedium: Double,
        priceLarge: Double,
        category: String,
        imageUrl: String,
        orders: Int
    ) {
        val cakeData = mapOf(
            "name" to name,
            "description" to description,
            "priceSmall" to priceSmall,
            "priceMedium" to priceMedium,
            "priceLarge" to priceLarge,
            "category" to category,
            "imageUrl" to imageUrl,
            "orders" to orders,
            "isDeleted" to false
        )

        val cakeRef = db.collection("cakes").document()

        cakeRef.set(cakeData)
            .addOnSuccessListener {
                _cakeState.value = CakeState.Success("Cake added successfully")
            }
            .addOnFailureListener { e ->
                _cakeState.value = CakeState.Error(e.message ?: "Failed to add cake")
            }
    }

    private fun updateCakeInFirestore(
        cakeId: String,
        name: String,
        description: String,
        priceSmall: Double,
        priceMedium: Double,
        priceLarge: Double,
        category: String,
        imageUrl: String
    ) {
        val updatedCakeData = mapOf(
            "name" to name,
            "description" to description,
            "priceSmall" to priceSmall,
            "priceMedium" to priceMedium,
            "priceLarge" to priceLarge,
            "category" to category,
            "imageUrl" to imageUrl,
            "isDeleted" to false
        )

        db.collection("cakes").document(cakeId)
            .set(updatedCakeData)
            .addOnSuccessListener {
                _cakeState.value = CakeState.Success("Cake updated successfully")
            }
            .addOnFailureListener { e ->
                _cakeState.value = CakeState.Error(e.message ?: "Failed to update cake")
            }
    }

    fun observeFavoriteStatus(userId: String, cakeId: String) {
        db.collection("favorites")
            .document("${userId}_$cakeId")
            .addSnapshotListener { snapshot, _ ->
                favoritesMap[cakeId] = snapshot?.exists() ?: false
            }
    }

    fun getFavoriteStatus(cakeId: String): Boolean {
        return favoritesMap[cakeId] ?: false
    }

    fun toggleFavoriteStatus(userId: String, cakeId: String) {
        val isFavorite = getFavoriteStatus(cakeId)
        val docRef = db.collection("favorites").document("${userId}_$cakeId")

        if (isFavorite) {
            docRef.delete()
        } else {
            docRef.set(mapOf("userId" to userId, "cakeId" to cakeId))
        }
    }

    fun getFavoriteCakes(userId: String): Flow<List<Cake>> = flow {
        val snapshot = db.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        val favoriteCakeIds = snapshot.documents.mapNotNull { it.getString("cakeId") }

        val favoriteCakes = db.collection("cakes")
            .whereIn("id", favoriteCakeIds)
            .get()
            .await()
            .documents.mapNotNull { it.toObject(Cake::class.java)?.copy(id = it.id) }

        emit(favoriteCakes)
    }
}

sealed class CakeState {
    object Idle : CakeState()
    object Loading : CakeState()
    data class Success(val message: String) : CakeState()
    data class Error(val message: String) : CakeState()
}
