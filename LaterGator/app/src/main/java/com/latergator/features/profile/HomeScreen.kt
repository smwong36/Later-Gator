package com.latergator.features.profile

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.latergator.features.auth.AuthManager

@Composable
fun HomeScreen(navController: NavHostController) {
    Text(text = "Welcome to Home!", modifier = Modifier.fillMaxSize())
}
//@Composable
//fun HomeScreen(navController: NavHostController) {
//    val context = LocalContext.current
//    var busy by remember { mutableStateOf(false) }
//
//    Column(
//        modifier = Modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Home")
//
//        if (busy) {
//            CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
//        } else {
//            Button(onClick = {
//                AuthManager.signOut(context as Activity)
//            }) {
//                Text(text = "Sign Out")
//            }
//        }
//    }
//}
