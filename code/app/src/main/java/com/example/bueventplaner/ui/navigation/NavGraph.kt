package com.example.bueventplaner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bueventplaner.ui.pages.EventDetailsView
import com.example.bueventplaner.ui.pages.EventListPage
import com.example.bueventplaner.ui.pages.LoginPage
import com.example.bueventplaner.ui.pages.SignupPage

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginPage(navController) }
        composable("signup") { SignupPage(navController) }
        composable("home") { /* TODO: Home Page Implementation */ }
        composable("event_list") { EventListPage(navController) }
        composable("event_details") { EventDetailsView(navController) }
    }
}
