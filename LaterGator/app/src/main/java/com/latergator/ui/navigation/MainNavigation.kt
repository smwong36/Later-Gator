package com.latergator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.latergator.features.pomodoro.PomodoroScreen
import com.latergator.features.profile.HomeScreen
import com.latergator.features.settings.SettingsScreen

@Composable
fun MainNavigation(modifier: Modifier = Modifier, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    AppNavHost(
        navController = navController,
        modifier = modifier,
        onSignOut = onSignOut
    )
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit
) {
    NavHost(navController = navController, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(navController, onSignOut = onSignOut) }
        composable("pomodoro") { PomodoroScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}
