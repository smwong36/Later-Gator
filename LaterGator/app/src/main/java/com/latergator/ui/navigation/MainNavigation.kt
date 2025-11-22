package com.latergator.ui.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.latergator.data.DatabaseHelper
import com.latergator.features.pomodoro.PomodoroScreen
import com.latergator.features.profile.CreateProfileScreen
import com.latergator.features.profile.HomeScreen
import com.latergator.features.settings.SelectAppsScreen
import com.latergator.features.settings.SettingsScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val context = LocalContext.current
    // Check if a profile exists to determine the start destination.
    // produceState runs this check once and caches the result.
    val startDestination by produceState<String?>(initialValue = null) {
        value = withContext(Dispatchers.IO) {
            if (DatabaseHelper(context).hasProfile()) "home" else "create_profile"
        }
    }

    // Display a loading indicator while we check for the profile.
    if (startDestination == null) {
        CircularProgressIndicator(modifier = modifier)
    } else {
        NavHost(navController = navController, startDestination = startDestination!!, modifier = modifier) {
            composable("home") { HomeScreen(navController, onSignOut = onSignOut) }
            composable("create_profile") { CreateProfileScreen(navController) }
            composable("pomodoro") { PomodoroScreen(navController) }
            composable("settings") { SettingsScreen(navController) }
            composable("select_apps") { SelectAppsScreen(navController) }
        }
    }
}
