package com.example.bueventplaner.ui.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.bueventplaner.R
import com.example.bueventplaner.ui.theme.BUEventPlanerTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginPage(navController: NavController) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            AuthPage(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                onLogin = { username, password ->
                    authenticateUser(username, password, navController)
                },
            )
        }
    )
}

private fun authenticateUser(email: String, password: String, navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val currentUser = auth.currentUser
                if (currentUser?.isEmailVerified == true) {
                    Toast.makeText(navController.context, "Login successful!", Toast.LENGTH_SHORT).show()
                    navController.navigate("event_list")
                } else {
                    Toast.makeText(navController.context, "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                    auth.signOut() // Sign out unverified users
                }
            } else {
                Toast.makeText(navController.context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthPage(
    navController: NavController,
    modifier: Modifier = Modifier,
    onLogin: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("caoyc2022@gmail.com") }
    var password by remember { mutableStateOf("123456") }
    var rememberMe by remember { mutableStateOf(false) }

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
                    text = "Log in",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Account") },
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text("Remember me")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onLogin(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC0000))
                ) {
                    Text("Log in", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text(" or ", style = MaterialTheme.typography.bodyMedium)
                    Divider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /* Google Login */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = "Google Login",
                            tint = Color(0xFF333333)
                        )
                    }
                    IconButton(onClick = { /* Other Login */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.school),
                            contentDescription = "Other Login",
                            tint = Color(0xFF333333)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Don’t have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    TextButton(
                        onClick = { navController.navigate("signup") },
                        modifier = Modifier.alignByBaseline(),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Sign up",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF3366CC)
                        )
                    }
                }
            }
        }
    }
}