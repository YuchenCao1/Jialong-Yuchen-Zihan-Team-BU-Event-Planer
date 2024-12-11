package com.example.bueventplaner

import android.app.Application
import com.example.bueventplaner.data.repository.EventDatabase

class EventPlannerApplication : Application() {
    val database by lazy { EventDatabase.getDatabase(this) }
}
