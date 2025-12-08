package com.latergator.features.pomodoro

import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.latergator.ui.navigation.POMODORO_GRAPH_ROUTE

/*
    Study setup screen
    - Allows user to set the number of study sessions
    - This screen works with the PomodoroScreens ViewModel class that handles the
    pomodoro countdown logic, screen looping, and cycles.
 */
@Composable
fun StudySetup(navController: NavHostController) {

    // Get ViewModel from the pomodoro navigation graph that controls the timer and session cycles
    val backStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(POMODORO_GRAPH_ROUTE)
    }
    val vm: PomodoroScreens = viewModel(backStackEntry)

    // stores the test field input for number of sessions, defaults to 1
    var sessionsText by remember { mutableStateOf("1") }
    //UI layout//
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Screen title
        Text(
            text = "Pomodoro Study Setup",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))
        // Text field for number of sessions
        TextField(
            value = sessionsText,
            onValueChange = { sessionsText = it },
            singleLine = true,
            label = { Text("Number of Sessions") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        // Button to start the study session
        // Converts the input to an integer and sets the session count
        Button(onClick = {
            val sessions = sessionsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
            vm.setSessionCount(sessions)

            navController.navigate("pomodoro") {
                launchSingleTop = true
            }
        }) {
            Text("Start Pomodoro")
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Button to go back to the home screen
        Button(onClick = {
            navController.navigate("home")
        }) {
            Text("Back")
        }
    }
}
