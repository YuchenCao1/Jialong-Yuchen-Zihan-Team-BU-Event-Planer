package com.example.bueventplaner.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SignupPage(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            AuthPage2(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                onRegister = { username, password, firstName, lastName ->
                    registerUser(username, password, firstName, lastName, navController)
                }
            )
        }
    )
}

private fun registerUser(email: String, password: String, firstName: String, lastName: String, navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.database.reference

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val currentUser = auth.currentUser
                if (userId != null && currentUser != null) {
                    val userMap = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email
                    )
                    database.child("users").child(userId).setValue(userMap)
                        .addOnSuccessListener {
                            currentUser.sendEmailVerification()
                                .addOnSuccessListener {
                                    Toast.makeText(navController.context, "Verification email sent. Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                                    navController.navigate("login")
                                }
                                .addOnFailureListener {
                                    Toast.makeText(navController.context, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(navController.context, "Failed to save user info.", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                val errorMessage = task.exception?.message ?: "Registration failed"
                Toast.makeText(navController.context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthPage2(
    modifier: Modifier = Modifier,
    navController: NavController,
    onRegister: (String, String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    // Using Column for layout and wrapping it inside a Scrollable Column to ensure it can scroll
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Make the layout scrollable
        verticalArrangement = Arrangement.Center
    ) {
        // Card to hold the UI components
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Title Text
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email TextField
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray,
                        focusedLabelColor = Color(0xFFCC0000),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Password TextField
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray,
                        focusedLabelColor = Color(0xFFCC0000),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // First Name TextField
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray,
                        focusedLabelColor = Color(0xFFCC0000),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Name TextField
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray,
                        focusedLabelColor = Color(0xFFCC0000),
                        unfocusedLabelColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register Button
                Button(
                    onClick = {
                        // Trigger register function when button is clicked
                        onRegister(email, password, firstName, lastName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) {
                    Text("Register", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation to Login page if user already has an account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    TextButton(
                        onClick = { navController.navigate("login") },
                        modifier = Modifier.alignByBaseline(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Log in",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF3366CC)
                        )
                    }
                }
            }
        }
    }
}
