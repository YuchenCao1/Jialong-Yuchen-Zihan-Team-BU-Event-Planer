package com.example.bueventplaner.data.model

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val password: String = "",
    val userEmail: String = "",
    val userImage: String = "",
    val userSavedEvents: List<Event> = emptyList()
)
