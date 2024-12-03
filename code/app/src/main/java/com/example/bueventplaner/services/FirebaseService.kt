package com.example.bueventplaner.services

import android.content.Context
import android.net.Uri
import com.example.bueventplaner.data.model.Event
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.bueventplaner.R
import com.example.bueventplaner.data.model.User
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseService {
    fun fetchEvents(context: Context, callback: (List<Event>) -> Unit) {
        val database = Firebase.database.reference.child("events")
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val events = snapshot.children.mapNotNull { eventSnapshot ->
                    eventSnapshot.key?.let { id ->
                        val event = eventSnapshot.getValue(Event::class.java)?.copy(id = id)
                        event?.let {
                            val drawableId = context.resources.getIdentifier(
                                it.photo,
                                "drawable",
                                context.packageName
                            )
                            it.copy(photo = if (drawableId != 0) drawableId.toString() else R.drawable.logo.toString()) // 替换为资源 ID
                        }
                    }
                }
                callback(events)
            } else {
                println("No events found in the database.")
                callback(emptyList())
            }
        }.addOnFailureListener { exception ->
            println("Failed to fetch events: ${exception.message}")
        }
    }

    fun fetchEventById(context: Context, eventId: String, callback: (Event?) -> Unit) {
        val database = Firebase.database.reference.child("events").child(eventId)
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val event = snapshot.getValue(Event::class.java)?.copy(id = eventId)
                event?.let {
                    val drawableId = context.resources.getIdentifier(
                        it.photo, // photo 是存储的 drawable 文件名
                        "drawable",
                        context.packageName
                    )
                    callback(it.copy(photo = if (drawableId != 0) drawableId.toString() else R.drawable.logo.toString()))
                } ?: callback(null)
            } else {
                println("Event with ID $eventId not found.")
                callback(null)
            }
        }.addOnFailureListener { exception ->
            println("Failed to fetch event: ${exception.message}")
            callback(null)
        }
    }

    fun fetchUsers(callback: (List<User>) -> Unit) {
        val database = Firebase.database.reference.child("users")
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                callback(users)
            } else {
                callback(emptyList())
            }
        }.addOnFailureListener {
            callback(emptyList())
        }
    }
    fun fetchUserByUsername(username: String, onUserFetched: (User?) -> Unit) {
        val databaseReference = Firebase.database.reference.child("users")
        databaseReference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // 遍历 users 节点，查找匹配的 username
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.username == username) {
                        onUserFetched(user)
                        return@addOnSuccessListener
                    }
                }
                println("User with username $username not found.")
                onUserFetched(null)
            } else {
                println("No users found in the database.")
                onUserFetched(null)
            }
        }.addOnFailureListener { exception ->
            println("Error fetching user: ${exception.message}")
            onUserFetched(null)
        }
    }


    suspend fun uploadUserProfileImage(uri: Uri, callback: (String?) -> Unit) {
        val fileName = "profilePics/${UUID.randomUUID()}.jpg"
        val ref = FirebaseStorage.getInstance().reference.child(fileName)

        try {
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            callback(downloadUrl)
        } catch (e: Exception) {
            callback(null)
        }
    }

    fun updateUserProfileImageUrl(userId: String, downloadUrl: String, callback: () -> Unit) {
        Firebase.database.reference.child("users").child(userId).child("userImage")
            .setValue(downloadUrl)
            .addOnSuccessListener { callback() }
            .addOnFailureListener { println("Failed to update profile image URL: ${it.message}") }
    }

    fun updateUserField(userId: String, field: String, value: Any) {
        Firebase.database.reference.child("users").child(userId).child(field)
            .setValue(value)
            .addOnFailureListener { println("Failed to update field $field: ${it.message}") }
    }
}