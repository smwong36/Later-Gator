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

@Composable
fun BreakScreen(navController: NavHostController) {

    // Shared ViewModel scoped to the navigation graph
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

    // Start break session once
    LaunchedEffect(Unit) {
        vm.startBreak()
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

        Text("Break Time", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            vm.stopTimer()
            navController.navigate("pomodoro") {
                launchSingleTop = true
            }
        }) {
            Text("Skip Break")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            vm.stopTimer()
            navController.navigate("home") {
                popUpTo(POMODORO_GRAPH_ROUTE) { inclusive = true }
            }
        }) {
            Text("Back to Home")
        }
    }

    // Handle break completion
    LaunchedEffect(vm.timeRemaining) {
        if (!vm.isRunning && vm.timeRemaining == 0) {

            // BREAK is done → go back to study
            navController.navigate("pomodoro") {
                launchSingleTop = true
            }
        }
    }
}
