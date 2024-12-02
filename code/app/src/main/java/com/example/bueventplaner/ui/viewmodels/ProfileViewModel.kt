package com.example.bueventplaner.ui.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.bueventplaner.data.model.Event
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class ProfileViewModel : ViewModel() {

    val profileImageUrl = mutableStateOf("")
    val firstName = mutableStateOf("")
    val lastName = mutableStateOf("")
    val points = mutableIntStateOf(0)

    val userSavedEvents = mutableStateListOf<Event>()
    val attendedEvents = mutableStateListOf<Event>()

    val isEditing = mutableStateOf(false)
    val isUploading = mutableStateOf(false)

    private val database = Firebase.database.reference
    private val currentUser = "currentUsername"

    init {
        fetchUserData()
    }

    fun toggleEditing() {
        isEditing.value = !isEditing.value
    }

    private fun fetchUserData() {
        val userRef = database.child("users").child(currentUser)
        userRef.get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                firstName.value = dataSnapshot.child("firstName").value as? String ?: "Unknown"
                lastName.value = dataSnapshot.child("lastName").value as? String ?: "Unknown"
                profileImageUrl.value = dataSnapshot.child("profileImageUrl").value as? String ?: ""
                points.value = (dataSnapshot.child("points").value as? Long)?.toInt() ?: 0

                val savedEvents = dataSnapshot.child("savedEvents").children.mapNotNull { snapshot ->
                    snapshotToEvent(snapshot)
                }
                val attendedEventsList = dataSnapshot.child("attendedEvents").children.mapNotNull { snapshot ->
                    snapshotToEvent(snapshot)
                }

                userSavedEvents.clear()
                userSavedEvents.addAll(savedEvents)

                attendedEvents.clear()
                attendedEvents.addAll(attendedEventsList)
            } else {
                println("User not found in database")
            }
        }.addOnFailureListener { exception ->
            println("Failed to fetch user data: ${exception.message}")
        }
    }

    fun saveUserData() {
        val userRef = database.child("users").child(currentUser)
        val userData = mapOf(
            "firstName" to firstName.value,
            "lastName" to lastName.value,
            "profileImageUrl" to profileImageUrl.value,
            "points" to points.value,
            "savedEvents" to userSavedEvents.map { eventToMap(it) },
            "attendedEvents" to attendedEvents.map { eventToMap(it) }
        )

        userRef.setValue(userData).addOnSuccessListener {
            println("User data updated successfully")
        }.addOnFailureListener { exception ->
            println("Failed to update user data: ${exception.message}")
        }
    }

    private fun snapshotToEvent(snapshot: com.google.firebase.database.DataSnapshot): Event? {
        val title = snapshot.child("title").value as? String ?: return null
        val description = snapshot.child("description").value as? String ?: return null
        return Event(title, description)
    }

    private fun eventToMap(event: Event): Map<String, Any> {
        return mapOf(
            "title" to event.title,
            "description" to event.description
        )
    }

    fun uploadImageToFirebase(uri: Uri, onComplete: (String?) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val fileName = "profile_images/${System.currentTimeMillis()}.jpg"
        isUploading.value = true

        val uploadTask = storageReference.child(fileName).putFile(uri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                profileImageUrl.value = downloadUrl.toString()
                isUploading.value = false
                onComplete(downloadUrl.toString())
            }.addOnFailureListener { exception ->
                println("Failed to get download URL: ${exception.message}")
                isUploading.value = false
                onComplete(null)
            }
        }.addOnFailureListener { exception ->
            println("Failed to upload image: ${exception.message}")
            isUploading.value = false
            onComplete(null)
        }
    }
}
