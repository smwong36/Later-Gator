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

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun handleSignInResult(context: Context, account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // On successful sign-in, save the user's name to the database.
                    account.displayName?.let { name ->
                        if (name.isNotBlank()) {
                            val dbHelper = DatabaseHelper(context)
                            dbHelper.saveUserProfile(name)
                        }
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun signOut(activity: Activity, googleSignInClient: GoogleSignInClient) {
        googleSignInClient.signOut().addOnCompleteListener {
            auth.signOut()

            // Clear the user profile from the database on sign out
            val dbHelper = DatabaseHelper(activity)
            dbHelper.saveUserProfile("")

            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            activity.startActivity(intent)
            activity.finish()
        }
    }
}
