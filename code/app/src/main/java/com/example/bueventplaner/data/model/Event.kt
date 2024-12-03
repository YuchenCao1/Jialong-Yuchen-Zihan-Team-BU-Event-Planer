package com.example.bueventplaner.data.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val eventUrl: String = "",
    val photo: String = "",
    val location: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val savedUsers: List<String> = emptyList()
)

