package com.example.bueventplaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.bueventplaner.ui.navigation.NavGraph
import androidx.compose.runtime.Composable
import android.content.Context
import com.example.bueventplaner.data.repository.EventDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCrashlyticsCollectionEnabled(true)

        crashlytics.setCustomKey("AppVersion", BuildConfig.VERSION_NAME)
        crashlytics.setCustomKey("Environment", "Production")
        setContent {
            MyApp(context = applicationContext)
        }
    }
}

@Composable
fun MyApp(context: Context) {
    val navController = rememberNavController()
    val database = EventDatabase.getDatabase(context)
    val eventDao = database.eventDao()

    MaterialTheme {
        Surface {
            NavGraph(navController = navController, eventDao = eventDao)
        }
    }
}
