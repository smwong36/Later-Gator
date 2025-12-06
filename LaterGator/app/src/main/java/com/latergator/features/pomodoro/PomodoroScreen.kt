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

@Composable
fun PomodoroScreen(navController: NavHostController) {

    // Shared ViewModel scoped to the navigation graph
    // We catch exception INSIDE remember block lambda to avoid Composable try-catch error
    val backStackEntry = remember(navController.currentBackStackEntry) {
        try {
            navController.getBackStackEntry(POMODORO_GRAPH_ROUTE)
        } catch (e: Exception) {
            null
        }
    }

    // If backStackEntry is null (e.g. graph destroyed), we exit early
    if (backStackEntry == null) return

    val vm: PomodoroScreens = viewModel(backStackEntry)

    // Start study session *once* per entry
    LaunchedEffect(Unit) {
        vm.startStudy()
    }

    val minutes = vm.timeRemaining / 60
    val seconds = vm.timeRemaining % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Study Time", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { vm.startStudy() }) {
                    Text("Restart Study")
                }

                Spacer(modifier = Modifier.height(8.dp))

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

            // STUDY session completed → mark progress
            vm.moveToNextSession()

            if (vm.completeStudyCycle()) {
                // All sessions finished → go home
                navController.navigate("home") {
                    popUpTo(POMODORO_GRAPH_ROUTE) { inclusive = true }
                }
            } else {
                // More sessions remain → take a break
                navController.navigate("break") {
                    launchSingleTop = true
                }
            }
        }
    }
}
