package com.example.bueventplaner.services

import android.content.Context
import com.example.bueventplaner.data.model.Event
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.tasks.Task
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.ktx.storage
import com.google.firebase.database.GenericTypeIndicator

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
    fun registerEventForUser(eventId: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            callback(false)
            return
        }

        val userId = currentUser.uid
        val userSavedEventsRef = Firebase.database.reference
            .child("users")
            .child(userId)
            .child("savedEvents")

        userSavedEventsRef.get().addOnSuccessListener { snapshot ->
            val savedEvents = snapshot.getValue() as? List<String> ?: emptyList()
            val updatedSavedEvents = savedEvents.toMutableList().apply {
                if (!contains(eventId)) add(eventId)
            }

            userSavedEventsRef.setValue(updatedSavedEvents).addOnSuccessListener {
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    fun unregisterEventForUser(eventId: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            callback(false)
            return
        }

        val userId = currentUser.uid
        val userSavedEventsRef = Firebase.database.reference
            .child("users")
            .child(userId)
            .child("savedEvents")

        userSavedEventsRef.get().addOnSuccessListener { snapshot ->
            val savedEvents = snapshot.getValue() as? List<String> ?: emptyList()
            val updatedSavedEvents = savedEvents.toMutableList().apply {
                remove(eventId)
            }

            userSavedEventsRef.setValue(updatedSavedEvents).addOnSuccessListener {
                callback(true)
            }.addOnFailureListener {
                callback(false)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }
}
