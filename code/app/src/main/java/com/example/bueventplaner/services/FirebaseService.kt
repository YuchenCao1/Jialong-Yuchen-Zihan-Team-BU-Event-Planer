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
import com.example.bueventplaner.data.repository.EventDatabase
import com.example.bueventplaner.data.repository.EventDao
import com.example.bueventplaner.data.model.EventEntity
import android.util.Log



object FirebaseService {
    suspend fun fetchEvents(context: Context, callback: (List<Event>) -> Unit) {
        val database = EventDatabase.getDatabase(context) // Get Room database instance
        val eventDao = database.eventDao()
        val TAG = "MyDebugTag"
        Log.d(TAG, "hhhhh111")

        // Step 1: Collect events from Room database using Flow
        val cachedEventsFlow = eventDao.getAllEvents()
        cachedEventsFlow.collect { cachedEvents ->
            Log.d(TAG, "Room cached events: $cachedEvents")

            if (cachedEvents.isNotEmpty()) {
                callback(cachedEvents.map { entity ->
                    Event(
                        id = entity.id,
                        title = entity.title,
                        description = entity.description,
                        eventUrl = entity.eventUrl,
                        photo = entity.photo,
                        location = entity.location,
                        startTime = entity.startTime,
                        endTime = entity.endTime,
                        savedUsers = entity.savedUsers
                    )
                })
            }
        }

        // Step 2: Fetch latest data from Firebase if network is available
        Firebase.database.reference.child("events").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d(TAG, "Fetched data: ${snapshot.value}")
                val tasks = mutableListOf<Task<Uri>>() // To store image URL fetch tasks
                val events = mutableListOf<Event>()    // To store final list of Event objects

                snapshot.children.forEach { eventSnapshot ->
                    val id = eventSnapshot.key ?: ""
                    val event = eventSnapshot.getValue(Event::class.java)?.copy(id = id)
                    if (event != null) {
                        // Fetch image URL from Firebase Storage
                        val storageRef = Firebase.storage.reference.child(event.photo)
                        val downloadTask = storageRef.downloadUrl
                        tasks.add(downloadTask)

                        // Update the photo field of the event after URL fetch succeeds
                        downloadTask.addOnSuccessListener { uri ->
                            events.add(event.copy(photo = uri.toString()))
                            // Check if all tasks are completed
                            if (events.size == snapshot.children.count()) {
                                // Step 3: Update Room database
                                eventDao.insertEvents(events.map { e ->
                                    EventEntity(
                                        id = e.id,
                                        title = e.title,
                                        description = e.description,
                                        eventUrl = e.eventUrl,
                                        photo = e.photo,
                                        location = e.location,
                                        startTime = e.startTime,
                                        endTime = e.endTime,
                                        savedUsers = e.savedUsers
                                    )
                                })
                                callback(events) // Return the latest events list
                            }
                        }.addOnFailureListener {
                            println("Failed to fetch image URL for event ${event.id}: ${it.message}")
                        }
                    }
                }

                // If no events exist, return an empty list
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


    suspend fun fetchEventById(context: Context, eventId: String, callback: (Event?) -> Unit) {
        val database = EventDatabase.getDatabase(context) // Get Room database instance
        val eventDao = database.eventDao()

        // Step 1: Collect the event from Room database using Flow
        val cachedEventFlow = eventDao.getEventById(eventId)
        cachedEventFlow.collect { cachedEvent ->
            if (cachedEvent != null) {
                callback(
                    Event(
                        id = cachedEvent.id,
                        title = cachedEvent.title,
                        description = cachedEvent.description,
                        location = cachedEvent.location,
                        startTime = cachedEvent.startTime,
                        endTime = cachedEvent.endTime,
                        photo = cachedEvent.photo
                    )
                )
            }
        }

        // Step 2: Fetch the latest data from Firebase if network is available
        Firebase.database.reference.child("events").child(eventId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val event = snapshot.getValue(Event::class.java)?.copy(id = eventId)
                event?.let { fetchedEvent ->
                    val storageRef = Firebase.storage.reference.child(fetchedEvent.photo)
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val updatedEvent = fetchedEvent.copy(photo = uri.toString())
                        // Step 3: Update Room database
                        eventDao.insertEvents(listOf(EventEntity(
                            id = updatedEvent.id,
                            title = updatedEvent.title,
                            description = updatedEvent.description,
                            eventUrl = updatedEvent.eventUrl,
                            photo = updatedEvent.photo,
                            location = updatedEvent.location,
                            startTime = updatedEvent.startTime,
                            endTime = updatedEvent.endTime,
                            savedUsers = updatedEvent.savedUsers
                        )))
                        callback(updatedEvent) // Return the latest event
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



    fun registerEventForUser(eventId: String, callback: (Boolean) -> Unit) {
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

    fun unregisterEventForUser(eventId: String, callback: (Boolean) -> Unit) {
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
