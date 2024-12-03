package com.example.bueventplaner.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bueventplaner.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ProfileViewModel : ViewModel() {

    val profileImageUrl = mutableStateOf("")
    val firstName = mutableStateOf("")
    val lastName = mutableStateOf("")

    private val storageReference = FirebaseStorage.getInstance().reference
    private val firestoreReference = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    val userSavedEvents = mutableStateListOf<Event>()
    val attendedEvents = mutableStateListOf<Event>()

    val isEditing = mutableStateOf(false)
    val isUploading = mutableStateOf(false)

    private val database = Firebase.database.reference


    private fun loadUserProfile() {
        currentUser?.uid?.let { userId ->
            firestoreReference.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstName.value = document.getString("firstName") ?: "John"
                        lastName.value = document.getString("lastName") ?: "Doe"
                        profileImageUrl.value = document.getString("profileImageUrl") ?: ""
                    }
                }
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


    fun updateProfileImageUrl(downloadUrl: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            // 更新 Firestore 中的 profileImageUrl 字段
            firestoreReference.collection("users").document(userId)
                .update("profileImageUrl", downloadUrl)
                .addOnSuccessListener {
                    profileImageUrl.value = downloadUrl
                    println("Profile image URL updated successfully.")
                }
                .addOnFailureListener { e ->
                    println("Failed to update profile image URL: ${e.message}")
                }
        }
    }

}