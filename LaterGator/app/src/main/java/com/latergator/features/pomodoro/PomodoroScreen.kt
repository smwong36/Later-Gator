package com.latergator.features.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.latergator.ui.theme.LaterGatorTheme

class PomodoroScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaterGatorTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PomodoroContent()
                }
            }
        }
    }
}

@Composable
fun PomodoroContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Pomodoro", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("25:00", style = MaterialTheme.typography.headlineLarge)

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { /* TODO: start timer */ }) {
                    Text("Start")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = { /* TODO: go back */ }) {
                    Text("Back")
                }
            }
        }
    }
}