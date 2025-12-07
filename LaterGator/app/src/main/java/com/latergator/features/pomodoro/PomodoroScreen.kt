package com.latergator.features.pomodoro

import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.latergator.ui.navigation.POMODORO_GRAPH_ROUTE
import java.util.Locale

/*
Pomodoro study mode screen
- Displays the countdown timer of 25 minutes
- This screen works with the PomodoroScreens ViewModel class that handles the
pomodoro countdown logic, screen looping, and cycles.
 */

@Composable
fun PomodoroScreen(navController: NavHostController) {
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

    // If the navigation graph entry is not available, we exit early
    if (backStackEntry == null) return

    // SHared view model for pomodoro screens
    val vm: PomodoroScreens = viewModel(backStackEntry)

    // Start study session once the screen is displayed
    //LaunchedEffect prevents restart
    LaunchedEffect(Unit) {
        vm.startStudy()
    }

    //convert time remaining to minutes and seconds for display
    val minutes = vm.timeRemaining / 60
    val seconds = vm.timeRemaining % 60

    // UI layout //
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Title for study screen//
        Text("Study Time", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        //Card has the timer an buttons
        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //timer
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                //restart timer button
                Button(onClick = { vm.startStudy() }) {
                    Text("Restart Study")
                }

                Spacer(modifier = Modifier.height(8.dp))

                //exit button that stops the timer and returns to home screen
                Button(onClick = {
                    vm.stopTimer()
                    navController.navigate("home") {
                        popUpTo(POMODORO_GRAPH_ROUTE) { inclusive = true }
                    }
                }) {
                    Text("Back to Home")
                }
            }
        }
    }

    // Handle study session completion
    LaunchedEffect(vm.timeRemaining) {
        if (!vm.isRunning && vm.timeRemaining == 0) {

            // marks the study session as complete
            vm.moveToNextSession()

            if (vm.completeStudyCycle()) {
                // All sessions finished novigate to home screen
                navController.navigate("home") {
                    popUpTo(POMODORO_GRAPH_ROUTE) { inclusive = true }
                }
            } else {
                // More sessions remain, move to break screen for next cycle
                navController.navigate("break") {
                    launchSingleTop = true
                }
            }
        }
    }
}
