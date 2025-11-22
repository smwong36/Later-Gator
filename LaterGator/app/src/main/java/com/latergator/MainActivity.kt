package com.latergator

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.latergator.data.DatabaseHelper
import com.latergator.features.auth.AuthManager
import com.latergator.ui.navigation.MainNavigation
import com.latergator.ui.theme.LaterGatorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }
            var signInError by remember { mutableStateOf<String?>(null) }

            val signInLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    if (task.isSuccessful) {
                        val account = task.result
                        if (account != null) {
                            AuthManager.handleSignInResult(this, account) { success, errorMessage ->
                                if (success) {
                                    isSignedIn = true
                                } else {
                                    signInError = errorMessage ?: getString(R.string.auth_error_firebase)
                                    Log.e("AUTH", signInError!!)
                                }
                            }
                        } else {
                            signInError = getString(R.string.auth_error_google_account_null)
                            Log.e("AUTH", signInError!!)
                        }
                    } else {
                        signInError = task.exception?.message ?: getString(R.string.auth_error_google_failed)
                        Log.e("AUTH", signInError!!, task.exception)
                    }
                } else {
                    signInError = getString(R.string.auth_error_generic, result.resultCode)
                    Log.e("AUTH", signInError!!)
                }
            }

            LaterGatorTheme {
                if (isSignedIn) {
                    // This effect runs once when the app starts with a user already signed in.
                    val context = LocalContext.current

                    val onSignOut = {
                        AuthManager.signOut(this, googleSignInClient)
                    }
                    Scaffold { padding ->
                        MainNavigation(
                            modifier = Modifier.padding(padding),
                            onSignOut = onSignOut
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (signInError != null) {
                            Text(text = signInError!!)
                        } else {
                            CircularProgressIndicator()
                            LaunchedEffect(Unit) {
                                signInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        }
                    }
                }
            }
        }
    }
}
