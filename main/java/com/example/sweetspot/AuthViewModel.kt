package com.example.sweetspot

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

val LocalAuthViewModel = staticCompositionLocalOf<AuthViewModel> {
    error("No AuthViewModel provided")
}


class AuthViewModel : ViewModel() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _userEmail = MutableLiveData<String?>()
    val userEmail: LiveData<String?> = _userEmail

    private val _fullName = MutableLiveData<String?>()
    val fullName: LiveData<String?> = _fullName

    private val _phoneNumber = MutableLiveData<String?>()
    val phoneNumber: LiveData<String?> = _phoneNumber

    init {
        checkAuthStatus()
    }

    fun refreshUserEmail() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _userEmail.value = currentUser.email
        }
    }

    fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated(null)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _userEmail.value = auth.currentUser?.email  // Set email on successful login
                    val userId = auth.currentUser?.uid
                    userId?.let { uid ->
                        // Fetch the user's role from Firestore
                        db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                val role = document.getString("role")
                                _authState.value = AuthState.Authenticated(role)
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Failed to retrieve role")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun register(email: String, password: String, fullName: String, phoneNumber: String) {
        if (email.isBlank() || password.isBlank() || fullName.isBlank() || phoneNumber.isBlank()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    userId?.let { uid ->
                        val userData = mapOf(
                            "email" to email,
                            "fullName" to fullName,
                            "phoneNumber" to phoneNumber,
                            "role" to "user"
                        )
                        db.collection("users").document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated("user")
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Failed to save user data")
                            }
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Registration failed")
                }
            }
    }

    fun getUserDetails(onResult: (String, String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("fullName") ?: ""
                    val phone = document.getString("phoneNumber") ?: ""
                    _fullName.value = name
                    _phoneNumber.value = phone
                    onResult(name, phone)
                }
                .addOnFailureListener {
                    _authState.value = AuthState.Error("Failed to retrieve user details")
                }
        }
    }

    fun updateUserDetails(fullName: String, phoneNumber: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userData = mapOf(
                "fullName" to fullName,
                "phoneNumber" to phoneNumber
            )

            db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener {
                    _fullName.value = fullName
                    _phoneNumber.value = phoneNumber
                    _authState.value = AuthState.Success("User details updated successfully")
                }
                .addOnFailureListener {
                    _authState.value = AuthState.Error("Failed to update user details")
                }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _userEmail.value = null
        _fullName.value = null
        _phoneNumber.value = null
    }
}

sealed class AuthState {
    data class Authenticated(val role: String?) : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
