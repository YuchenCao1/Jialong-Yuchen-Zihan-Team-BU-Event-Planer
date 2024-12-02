package com.example.bueventplaner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.bueventplaner.ui.pages.LoginPage
import com.example.bueventplaner.ui.pages.SignupPage
import com.example.bueventplaner.ui.pages.OnboardingPage
import com.example.bueventplaner.ui.pages.ProfilePage

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "profile") {
        composable("login") { LoginPage(navController) }
        composable("signup") { SignupPage(navController) }
        composable("onboarding") { OnboardingPage(navController) }
        composable("profile") { ProfilePage(navController) }
    }
}
