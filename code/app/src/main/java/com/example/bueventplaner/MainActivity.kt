package com.example.bueventplaner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.bueventplaner.ui.navigation.NavGraph
import com.example.bueventplaner.ui.theme.BUEventPlanerTheme
import androidx.compose.runtime.Composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    BUEventPlanerTheme {
        Surface {
            NavGraph(navController = navController)
        }
    }
}
