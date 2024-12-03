package com.example.bueventplaner.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bueventplaner.data.model.Event
import com.example.bueventplaner.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileViewModel : ViewModel() {

    val userProfile = mutableStateOf(
        User(
            username = "",
            password = "",
            firstName = "",
            lastName = "",
            userProfileURL = "",
            userBUID = "",
            userEmail = "",
            userSchool = "",
            userYear = "",
            userImage = "",
            userSavedEvents = emptyList()
        )
    )

    private val storageReference = FirebaseStorage.getInstance().reference
    private val databaseReference = Firebase.database.reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    val isEditing = mutableStateOf(false)

    init {
        loadUserProfile()
    }


    private fun loadUserProfile() {
        val username = FirebaseAuth.getInstance().currentUser?.email?.substringBefore('@') ?: return
        databaseReference.child("users").child(username).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val loadedUser = snapshot.getValue(User::class.java)
                    if (loadedUser != null) {
                        userProfile.value = loadedUser
                    }
                } else {
                    println("User not found in database.")
                }
            }
            .addOnFailureListener { e ->
                println("Failed to load user profile: ${e.message}")
            }
    }


    suspend fun uploadImageToFirebase(uri: Uri, onResult: (String?) -> Unit) {
        val fileName = "profilePics/${UUID.randomUUID()}.jpg"
        val ref = storageReference.child(fileName)

        try {
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            onResult(downloadUrl)
        } catch (e: Exception) {
            onResult(null)
        }
    }

    fun updateUserProfileImageUrl(downloadUrl: String) {
        currentUser?.uid?.let { userId ->
            databaseReference.child("users").child(userId)
                .child("userImage")
                .setValue(downloadUrl)
                .addOnSuccessListener {
                    userProfile.value = userProfile.value.copy(userImage = downloadUrl)
                    println("Profile image URL updated successfully.")
                }
                .addOnFailureListener { e ->
                    println("Failed to update profile image URL: ${e.message}")
                }
        }
    }
}
