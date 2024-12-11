package com.example.bueventplaner.services

import android.content.Context
import com.example.bueventplaner.data.model.Event
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Task
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object FirebaseService {
    fun fetchEvents(context: Context, callback: (List<Event>) -> Unit) {
        val database = Firebase.database.reference.child("events")
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val tasks = mutableListOf<Task<Uri>>() // Used to store all download tasks
                val events = mutableListOf<Event>()    // Used to store the final list of Event objects

                snapshot.children.forEach { eventSnapshot ->
                    val id = eventSnapshot.key ?: ""
                    val event = eventSnapshot.getValue(Event::class.java)?.copy(id = id)
                    if (event != null) {
                        // Fetch the image URL from Firebase Storage
                        val storageRef = Firebase.storage.reference.child(event.photo)
                        val downloadTask = storageRef.downloadUrl
                        tasks.add(downloadTask)

                        // Update the photo field of the event after download succeeds
                        downloadTask.addOnSuccessListener { uri ->
                            events.add(event.copy(photo = uri.toString()))
                            // Check if all tasks are completed
                            if (events.size == snapshot.children.count()) {
                                callback(events)
                            }
                        }.addOnFailureListener {
                            println("Failed to fetch image URL for event ${event.id}: ${it.message}")
                        }
                    }
                }

                // If no events exist, directly return an empty list
                if (tasks.isEmpty()) {
                    callback(emptyList())
                }
            } else {
                println("No events found in the database.")
                callback(emptyList())
            }
        }.addOnFailureListener { exception ->
            println("Failed to fetch events: ${exception.message}")

            callback(emptyList())
        }
    }

    fun fetchEventById(context: Context, eventId: String, callback: (Event?) -> Unit) {
        val database = Firebase.database.reference.child("events").child(eventId)
        database.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val event = snapshot.getValue(Event::class.java)?.copy(id = eventId)
                event?.let { fetchedEvent ->
                    val storageRef = Firebase.storage.reference.child(fetchedEvent.photo)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Replace the photo field with the downloaded URL
                        callback(fetchedEvent.copy(photo = uri.toString()))
                    }
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
    fun addEventForUser(eventId: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            callback(false)
            return
        }

        val userId = currentUser.uid
        val database = Firebase.database.reference

        // Add event to user's savedEvents
        val userSavedEventsRef = database.child("users").child(userId).child("savedEvents")
        userSavedEventsRef.get().addOnSuccessListener { snapshot ->
            val savedEvents = snapshot.value as? List<String> ?: emptyList()
            val updatedSavedEvents = savedEvents.toMutableList().apply {
                if (!contains(eventId)) add(eventId)
            }

            userSavedEventsRef.setValue(updatedSavedEvents).addOnSuccessListener {
                // Add user to event's savedUsers
                val eventSavedUsersRef = database.child("events").child(eventId).child("savedUsers")
                eventSavedUsersRef.get().addOnSuccessListener { eventSnapshot ->
                    val savedUsers = eventSnapshot.value as? List<String> ?: emptyList()
                    val updatedSavedUsers = savedUsers.toMutableList().apply {
                        if (!contains(userId)) add(userId)
                    }

                    eventSavedUsersRef.setValue(updatedSavedUsers).addOnSuccessListener {
                        callback(true)
                    }.addOnFailureListener { callback(false) }
                }.addOnFailureListener { callback(false) }
            }.addOnFailureListener { callback(false) }
        }.addOnFailureListener { callback(false) }
    }

    fun fetchUserFullName(callback: (String, String) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            callback("Unknown", "Unknown")
            return
        }

        val userId = currentUser.uid
        val userRef = Firebase.database.reference
            .child("users")
            .child(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val firstName = snapshot.child("firstName").getValue(String::class.java) ?: "Unknown"
                val lastName = snapshot.child("lastName").getValue(String::class.java) ?: "Unknown"
                callback(firstName, lastName)
            } else {
                callback("Unknown", "Unknown")
            }
        }.addOnFailureListener {
            callback("Unknown", "Unknown")
        }
    }

    fun removeEventForUser(eventId: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            callback(false)
            return
        }

        val userId = currentUser.uid
        val database = Firebase.database.reference

        // Remove event from user's savedEvents
        val userSavedEventsRef = database.child("users").child(userId).child("savedEvents")
        userSavedEventsRef.get().addOnSuccessListener { snapshot ->
            val savedEvents = snapshot.value as? List<String> ?: emptyList()
            val updatedSavedEvents = savedEvents.toMutableList().apply {
                remove(eventId)
            }

            userSavedEventsRef.setValue(updatedSavedEvents).addOnSuccessListener {
                // Remove user from event's savedUsers
                val eventSavedUsersRef = database.child("events").child(eventId).child("savedUsers")
                eventSavedUsersRef.get().addOnSuccessListener { eventSnapshot ->
                    val savedUsers = eventSnapshot.value as? List<String> ?: emptyList()
                    val updatedSavedUsers = savedUsers.toMutableList().apply {
                        remove(userId)
                    }

                    eventSavedUsersRef.setValue(updatedSavedUsers).addOnSuccessListener {
                        callback(true)
                    }.addOnFailureListener { callback(false) }
                }.addOnFailureListener { callback(false) }
            }.addOnFailureListener { callback(false) }
        }.addOnFailureListener { callback(false) }
    }


    suspend fun uploadImageToFirebase(uri: Uri, onResult: (String?) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val fileName = "profilePics/${userId}.jpg"
        val ref = storageReference.child(fileName)

        try {
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            onResult(downloadUrl)
        } catch (e: Exception) {
            println("Failed to upload image: ${e.message}")
            onResult(null)
        }
    }


    fun updateProfileImageUrl(downloadUrl: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { userId ->
            val userRef = databaseReference.child("users").child(userId)

            userRef.child("profileImageUrl").setValue(downloadUrl)
                .addOnSuccessListener {
                    println("Profile image URL updated successfully.")
                }
                .addOnFailureListener { e ->
                    println("Failed to update profile image URL: ${e.message}")
                }
        }
    }

    fun updateUserName(firstName: String, lastName: String, onResult: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            onResult(false)
            return
        }

        val userId = currentUser.uid
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        val updates = mapOf(
            "firstName" to firstName,
            "lastName" to lastName
        )

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
    
    fun fetchUserSavedEvents(userSavedEvents: MutableList<Event>) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val userId = currentUser.uid
        val database = FirebaseDatabase.getInstance().reference
        val storage = FirebaseStorage.getInstance().reference

        database.child("users").child(userId).child("savedEvents").get()
            .addOnSuccessListener { snapshot ->
                val savedEventIds = snapshot.value as? List<String> ?: emptyList()
                if (savedEventIds.isEmpty()) {
                    return@addOnSuccessListener
                }

                savedEventIds.forEach { eventId ->
                    database.child("events").child(eventId).get()
                        .addOnSuccessListener { eventSnapshot ->
                            val event = eventSnapshot.getValue(Event::class.java)
                            if (event != null) {
                                if (!event.photo.startsWith("http")) {
                                    val storageRef = storage.child(event.photo)
                                    storageRef.downloadUrl
                                        .addOnSuccessListener { url ->
                                            userSavedEvents.add(event.copy(photo = url.toString()))
                                        }
                                        .addOnFailureListener {
                                            println("Failed to fetch download URL for ${event.photo}")
                                        }
                                } else {
                                    userSavedEvents.add(event)
                                }
                            }
                        }
                        .addOnFailureListener {
                            println("Failed to fetch event: ${it.message}")
                        }
                }
            }
            .addOnFailureListener {
                println("Failed to fetch saved events: ${it.message}")
            }
    }

}
