package com.example.bueventplaner.ui.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bueventplaner.ui.theme.BUEventPlanerTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@Composable
fun SignupPage(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            AuthPage2(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                onRegister = { username, password ->
                    registerUser(username, password, navController)
                }
            )
        }
    )
}

private fun registerUser(username: String, password: String, navController: NavController) {
    val database = Firebase.database.reference.child("users").child(username)
    val userData = mapOf(
        "password" to password
    )
    database.get().addOnSuccessListener {
        if (it.exists()) {
            Toast.makeText(navController.context, "User exists!", Toast.LENGTH_SHORT).show()
        } else {
            database.setValue(userData).addOnSuccessListener {
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


@Composable
fun AuthPage2(
    modifier: Modifier = Modifier,
    navController: NavController,
    onRegister: (String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("This is Register!")
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onRegister(username, password)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Switch to Login")
        }
    }
}


