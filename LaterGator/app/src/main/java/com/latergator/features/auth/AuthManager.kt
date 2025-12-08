package com.latergator.features.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.latergator.MainActivity
import com.latergator.data.DatabaseHelper

// Helper object for authentication and authorization
object AuthManager {
    private val auth = FirebaseAuth.getInstance()
    // Handles the result of a Google Sign-In request and signs the user in with Firebase.
    fun handleSignInResult(context: Context, account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
    // Signs the user out of Firebase and the app
    fun signOut(activity: Activity, googleSignInClient: GoogleSignInClient) {
        googleSignInClient.signOut().addOnCompleteListener {
            auth.signOut()

            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }
}
