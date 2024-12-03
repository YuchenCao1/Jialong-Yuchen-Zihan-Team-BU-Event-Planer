package com.example.bueventplaner.ui.pages

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bueventplaner.data.model.User
import com.example.bueventplaner.ui.theme.BUEventPlanerTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

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

private fun registerUser(
    username: String,
    password: String,
    firstName: String,
    lastName: String,
    navController: NavController
) {
    val database = Firebase.database.reference.child("users").child(username)


    val user = User(
        username = username,
        firstName = firstName,
        lastName = lastName,
        password = password,
        userProfileURL = "default", // 默认值为空
        userEmail = "default", // 默认值为空
        userBUID = "default", // 默认值为空
        userSchool = "default", // 默认值为空
        userYear = "default", // 默认值为空
        userImage = "default", // 默认值为空
        userSavedEvents = emptyList() // 空的事件列表
    )

    database.get().addOnSuccessListener {
        if (it.exists()) {
            Toast.makeText(navController.context, "User exists!", Toast.LENGTH_SHORT).show()
        } else {
            // 上传完整的用户数据到 Firebase
            database.setValue(user).addOnSuccessListener {
                Toast.makeText(navController.context, "Registration successful!", Toast.LENGTH_SHORT).show()
                navController.navigate("login")
            }.addOnFailureListener {
                Toast.makeText(navController.context, "Failed to register user", Toast.LENGTH_SHORT).show()
            }
        }
    }.addOnFailureListener { exception ->
        Toast.makeText(navController.context, "Failed to connect to database: ${exception.message}", Toast.LENGTH_SHORT).show()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthPage2(
    modifier: Modifier = Modifier,
    navController: NavController,
    onRegister: (String, String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
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

                // First Name
                TextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Name
                TextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.White,
                        focusedIndicatorColor = Color(0xFFCC0000),
                        unfocusedIndicatorColor = Color.Gray
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
                        unfocusedIndicatorColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register button
                Button(
                    onClick = {
                        onRegister(username, password, firstName, lastName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) {
                    Text("Register", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Switch to login
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
