package com.latergator.features.pomodoro

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.latergator.ui.navigation.POMODORO_GRAPH_ROUTE
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.util.Locale
/*
Break screen for Pomodoro study mode
- Displays the countdown timer of 5 minutes
- This screen works with the PomodoroScreens ViewModel class that handles the
pomodoro countdown logic, screen looping, and cycles.
 */
@Composable
fun BreakScreen(navController: NavHostController) {

    /*
   Retrieve ViewModel from the pomodoro navigation graph
   try/catch wrapped inside a remember block crashing caused by navigation graph destruction.
    */
    val backStackEntry = remember(navController.currentBackStackEntry) {
        try {
            navController.getBackStackEntry(POMODORO_GRAPH_ROUTE)
        } catch (e: Exception) {
            null
        }
    }

    // If backStackEntry is null, graph entry is missing, we exit early
    if (backStackEntry == null) return

    //shared viewmodel containing pomodoro session
    val vm: PomodoroScreens = viewModel(backStackEntry)

    // Start break session once the screen is displayed
    LaunchedEffect(Unit) {
        vm.startBreak()
    }
    //convert time remaining to minutes and seconds for display
    val minutes = vm.timeRemaining / 60
    val seconds = vm.timeRemaining % 60

    //UI layout//
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Break Time", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        //Break session title
        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))
        //timer
        Button(onClick = {
            vm.stopTimer()
            navController.navigate("pomodoro") {
                launchSingleTop = true
            }
        }) {
            Text("Skip Break")
        }

        Spacer(modifier = Modifier.height(16.dp))
        // skip break button that stops the timer and returns to new session
        Button(onClick = {
            vm.stopTimer()
            navController.navigate("home") {
                popUpTo(POMODORO_GRAPH_ROUTE) { inclusive = true }
            }
        }) {
            Text("Back to Home")
        }
    }

    /* Handle break completion logic
        - When timer hits 0 and isrunning = false, break session ends
     */
    LaunchedEffect(vm.timeRemaining) {
        if (!vm.isRunning && vm.timeRemaining == 0) {

            // Break session end and goes back to study
            navController.navigate("pomodoro") {
                launchSingleTop = true
            }
        }
    }
}
