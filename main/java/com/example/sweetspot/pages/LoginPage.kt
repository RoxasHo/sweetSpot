package com.example.sweetspot.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.sweetspot.AuthViewModel
import com.example.sweetspot.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.text.TextStyle


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginPage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastSentEmail by remember { mutableStateOf("") }

    val backgroundImage = painterResource(id = R.drawable.background)

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = backgroundImage,
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Login Page",
                        fontSize = 28.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                        },
                        label = { Text(text = "Email", color = Color.Gray) },
                        placeholder = { Text(text = "Enter your email", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        ),
                        textStyle = TextStyle(color = Color.Black),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(text = "Password", color = Color.Gray) },
                        placeholder = { Text(text = "Enter your password", color = Color.Gray) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                handleLogin(
                                    email,
                                    password,
                                    auth,
                                    db,
                                    navController,
                                    authViewModel
                                ) { errorMessage = it }
                            }
                        ),
                        textStyle = TextStyle(color = Color.Black),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            cursorColor = Color.Black,
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Black,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    errorMessage?.let {
                        Text(text = it, color = Color.Red, modifier = Modifier.padding(8.dp))
                    }

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email and Password cannot be empty."
                            } else {
                                handleLogin(
                                    email,
                                    password,
                                    auth,
                                    db,
                                    navController,
                                    authViewModel
                                ) { errorMessage = it }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA726),
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "Login", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = {
                        navController.navigate("register")
                    }) {
                        Text(text = "Don't have an account? Signup", color = Color.Gray)
                    }

                    TextButton(
                        onClick = {
                            if (email.isBlank()) {
                                errorMessage = "Please enter your email and click \"Forgot Password\" again to reset the password."
                            } else {
                                handleForgotPassword(email, auth, context) {
                                    lastSentEmail = email
                                    errorMessage = it
                                }
                            }
                        }
                    ) {
                        Text(text = "Forgot Password?", color = Color.Gray)
                    }
                }
            }
        }
    }
}

fun handleLogin(
    email: String,
    password: String,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    navController: NavController,
    authViewModel: AuthViewModel,
    setErrorMessage: (String?) -> Unit
) {
    if (email.isBlank() || password.isBlank()) {
        setErrorMessage("Email and password cannot be empty")
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                authViewModel.refreshUserEmail()

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val role = document.getString("role") ?: "user"
                                val fullName = document.getString("fullName") ?: "No Name"
                                val phoneNumber = document.getString("phoneNumber") ?: "No Number"

                                authViewModel.updateUserDetails(fullName, phoneNumber)

                                when (role) {
                                    "admin" -> navController.navigate("adminHome")
                                    "user" -> navController.navigate("userHome")
                                    else -> setErrorMessage("Invalid role. Please contact support.")
                                }
                            } else {
                                setErrorMessage("User data does not exist in Firestore.")
                            }
                        }
                        .addOnFailureListener {
                            setErrorMessage("Error retrieving user data: ${it.message}")
                        }
                } else {
                    setErrorMessage("Failed to retrieve user details.")
                }
            } else {
                setErrorMessage(task.exception?.message ?: "Login failed")
            }
        }
}

fun handleForgotPassword(
    email: String,
    auth: FirebaseAuth,
    context: android.content.Context,
    setErrorMessage: (String?) -> Unit
) {
    if (email.isBlank()) {
        setErrorMessage("Please enter your email to reset the password.")
        return
    }

    auth.sendPasswordResetEmail(email)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Password reset email sent to $email.", Toast.LENGTH_SHORT).show()
                setErrorMessage(null)
            } else {
                setErrorMessage(task.exception?.message ?: "Failed to send reset email.")
            }
        }
}
