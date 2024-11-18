package com.example.bueventplaner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.bueventplaner.ui.theme.BUEventPlanerTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BUEventPlanerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    content = { paddingValues ->
                        AuthPage(
                            modifier = Modifier.padding(paddingValues),
                            onLogin = { username, password ->
                                authenticateUser(username, password)
                            },
                            onRegister = { username, password ->
                                registerUser(username, password)
                            }
                        )
                    }
                )
            }
        }
    }

    private fun authenticateUser(username: String, password: String) {
        val database = Firebase.database.reference.child("users").child(username)
        database.get().addOnSuccessListener {
            val storedPassword = it.child("password").value
            if (storedPassword == password) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to connect to database", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser(username: String, password: String) {
        val database = Firebase.database.reference.child("users").child(username)
        val userData = mapOf(
            "username" to username,
            "password" to password
        )

        database.setValue(userData).addOnSuccessListener {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to register user", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun AuthPage(
    modifier: Modifier = Modifier,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
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
                if (isLoginMode) {
                    onLogin(username, password)
                } else {
                    onRegister(username, password)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoginMode) "Login" else "Register")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { isLoginMode = !isLoginMode },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoginMode) "Switch to Register" else "Switch to Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthPagePreview() {
    BUEventPlanerTheme {
        AuthPage(
            onLogin = { _, _ -> },
            onRegister = { _, _ -> }
        )
    }
}
