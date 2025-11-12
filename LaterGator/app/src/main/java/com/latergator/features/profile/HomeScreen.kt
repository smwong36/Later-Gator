package com.latergator.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.latergator.R
import com.latergator.data.DatabaseHelper
import com.latergator.ui.components.NavigationButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavHostController, onSignOut: () -> Unit) {
    val context = LocalContext.current
    var userName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val dbHelper = DatabaseHelper(context)
            userName = dbHelper.getUserProfileName()
        }
    }

    val welcomeMessage = remember(userName) {
        val firstName = userName?.split(" ")?.firstOrNull()
        if (!firstName.isNullOrBlank()) {
            "Welcome to Later Gator, $firstName"
        } else {
            "Welcome to Later Gator"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = welcomeMessage, modifier = Modifier.padding(bottom = 32.dp))

        NavigationButton(
            text = stringResource(R.string.pomodoro_timer_button),
            onClick = { navController.navigate("pomodoro") }
        )

        NavigationButton(
            text = stringResource(R.string.settings_button),
            onClick = { navController.navigate("settings") }
        )

        Button(onClick = onSignOut, modifier = Modifier.padding(top = 32.dp)) {
            Text(stringResource(R.string.sign_out))
        }
    }
}
