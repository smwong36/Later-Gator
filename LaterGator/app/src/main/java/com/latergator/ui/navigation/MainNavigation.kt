package com.latergator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.latergator.features.profile.HomeScreen
import com.latergator.features.pomodoro.PomodoroScreen
// Add more imports here as you add screens

@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("pomodoro") { PomodoroScreen() }
        // composable("settings") { SettingsScreen() }
        // Add more composable routes as needed
    }
}
