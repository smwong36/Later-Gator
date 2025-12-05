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

@Composable
fun StudySetup(navController: NavHostController) {

    // Get shared ViewModel scoped to pomodoro_graph
    val backStackEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry(POMODORO_GRAPH_ROUTE)
    }
    val vm: PomodoroScreens = viewModel(backStackEntry)

    var sessionsText by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pomodoro Study Setup",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextField(
            value = sessionsText,
            onValueChange = { sessionsText = it },
            singleLine = true,
            label = { Text("Number of Sessions") }
        )

        Spacer(modifier = Modifier.height(24.dp))

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

        Button(onClick = {
            navController.navigate("home")
        }) {
            Text("Back")
        }
    }
}
