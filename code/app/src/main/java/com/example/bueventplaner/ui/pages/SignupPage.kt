package com.example.bueventplaner.ui.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bueventplaner.ui.theme.BUEventPlanerTheme
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
                if (userId != null) {
                    val userMap = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email
                    )
                    database.child("users").child(userId).setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(navController.context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            navController.navigate("login")
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Sign up",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Username
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

                // Password
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
                // register button
                Button(
                    onClick = {
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

                // switch to login
                TextButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Already have an account? Log in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3366CC)
                    )
                }
            }
        }
    }
}