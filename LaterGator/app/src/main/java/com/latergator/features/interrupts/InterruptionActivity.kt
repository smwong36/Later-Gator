package com.latergator.features.interrupts

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.latergator.data.DatabaseHelper
import com.latergator.ui.theme.LaterGatorTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Activity for the blocked app screen
class InterruptionActivity : ComponentActivity() {
// action for closing the app
    companion object {
        const val ACTION_CLOSE_APP = "com.latergator.CLOSE_APP"
    }
    // Set up the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Disable back press using the modern approach
        onBackPressedDispatcher.addCallback(this) {
            // Do nothing to prevent the user from backing out
        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        setFinishOnTouchOutside(false)
        // Get the blocked package from the intent
        val blockedPackage = intent.getStringExtra("BLOCKED_PACKAGE") ?: "this app"
        // Set up the UI
        setContent {
            LaterGatorTheme {
                BlockedAppScreen(
                    packageName = blockedPackage,
                    onClose = { closeApp() },
                    onSnooze = { finish() } // Just finish activity to stay in app
                )
            }
        }
    }
    // Close the app
    private fun closeApp() {
        val intent = Intent(ACTION_CLOSE_APP)
        intent.setPackage(packageName) // Explicitly target this app
        sendBroadcast(intent)
        
        // Delay finish to allow Global Action Home to execute first.
        // This prevents the underlying app from flashing and re-triggering the block.
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 500)
    }
}
// Screen for blocked app
@Composable
fun BlockedAppScreen(
    packageName: String, 
    onClose: () -> Unit,
    onSnooze: () -> Unit
) {
    // Initialize dependencies
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val scope = rememberCoroutineScope()

    val appIdState = remember { mutableStateOf<Int?>(null) }
    val snoozesRemaining = remember { mutableIntStateOf(0) }

    // Get App Label
    val packageManager = context.packageManager
    val appLabel = remember(packageName) {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName
        }
    }

    // Safely load data from the database on a background thread
    LaunchedEffect(packageName) {
        withContext(Dispatchers.IO) {
            val id = dbHelper.getAppId(packageName)
            if (id != -1) {
                appIdState.value = id
                snoozesRemaining.intValue = dbHelper.getRemainingSnoozes("per_app", id)
            }
        }
    }

    val appId = appIdState.value
    // Ui for blocked app
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .background(Color.White)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You've reached your limit for",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Text(
                text = appLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close App")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    appId?.let { id ->
                        scope.launch {
                            // Run DB write on IO thread and wait for it
                            withContext(Dispatchers.IO) {
                                dbHelper.logSnoozeUsed(id, "per_app")
                            }
                            // Then update UI/Finish on Main thread
                            snoozesRemaining.intValue--
                            onSnooze() 
                        }
                    }
                },
                enabled = snoozesRemaining.intValue > 0 && appId != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (snoozesRemaining.intValue > 0) MaterialTheme.colorScheme.primary
                    else Color.LightGray
                )
            ) {
                Text("Snooze (${snoozesRemaining.intValue} left)")
            }
        }
    }
}
