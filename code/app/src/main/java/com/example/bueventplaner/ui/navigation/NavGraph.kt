package com.example.bueventplaner.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bueventplaner.data.repository.EventDatabase
import com.example.bueventplaner.ui.pages.EventDetailsView
import com.example.bueventplaner.ui.pages.EventListPage
import com.example.bueventplaner.ui.pages.LoginPage
import com.example.bueventplaner.ui.pages.SignupPage
import com.example.bueventplaner.ui.pages.OnboardingPage
import com.example.bueventplaner.ui.pages.ProfilePage
import com.example.bueventplaner.data.repository.EventDao
import com.example.bueventplaner.ui.pages.CalendarRoute


@Composable
fun NavGraph(navController: NavHostController, eventDao: EventDao) {
    NavHost(navController = navController, startDestination = "onboarding") {
        composable("login") { LoginPage(navController) }
        composable("signup") { SignupPage(navController) }
        composable("event_list") { EventListPage(navController, eventDao) }
        composable("event_details/{eventId}") { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            EventDetailsView(navController, eventId, eventDao)
        }
        composable("onboarding") { OnboardingPage(navController) }
        composable("profile") { ProfilePage(navController = navController) }
        composable("calendar") { CalendarRoute(navController) }
    }
}