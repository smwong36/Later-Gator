package com.latergator.features.interrupts.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.latergator.data.DatabaseHelper
import com.latergator.features.interrupts.InterruptionActivity

class BlockingAccessibilityService : AccessibilityService() {

    private val tag = "BlockingService"
    private lateinit var dbHelper: DatabaseHelper
    private var currentForegroundApp: String? = null
    
    // Handler for continuous monitoring
    private val monitorHandler = Handler(Looper.getMainLooper())
    private val monitorIntervalMs = 1000L * 10 // Check every 10 seconds

    private val monitorRunnable = object : Runnable {
        override fun run() {
            currentForegroundApp?.let { packageName ->
                checkAndBlockIfLimitReached(packageName)
            }
            // Schedule next check
            monitorHandler.postDelayed(this, monitorIntervalMs)
        }
    }

    private val closeAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(tag, "Received close app broadcast. Performing GLOBAL_ACTION_HOME")
            val result = performGlobalAction(GLOBAL_ACTION_HOME)
            if (!result) {
                Log.e(tag, "Failed to perform GLOBAL_ACTION_HOME")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "onCreate: Accessibility Service is being created.")
        dbHelper = DatabaseHelper(this)
        // Use ContextCompat to safely handle receiver registration across API levels
        ContextCompat.registerReceiver(
            this, 
            closeAppReceiver, 
            IntentFilter(InterruptionActivity.ACTION_CLOSE_APP), 
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            
            if (packageName == null || packageName == currentForegroundApp) {
                return
            }

            // 1. Cleanup Previous App
            currentForegroundApp?.let { oldApp ->
                val oldAppId = dbHelper.getAppId(oldApp)
                if (oldAppId != -1) {
                    dbHelper.endUsageSession(oldAppId)
                }
            }

            // Stop monitoring the old app
            monitorHandler.removeCallbacks(monitorRunnable)

            // 2. Setup New App
            currentForegroundApp = packageName

            // Skip our own app and launcher
            if (packageName == this.packageName || isLauncher(packageName)) {
                return
            }

            // 3. Check if New App is Tracked
            val trackedApps = dbHelper.getTrackedApps()
            
            if (trackedApps.containsKey(packageName)) {
                val newAppId = dbHelper.getAppId(packageName)
                
                if (newAppId != -1) {
                    // Start Session
                    dbHelper.incrementUsage(newAppId)
                    
                    // Initial Block Check (Immediate)
                    if (!checkAndBlockIfLimitReached(packageName)) {
                        // Start continuous monitoring if not immediately blocked
                        monitorHandler.postDelayed(monitorRunnable, monitorIntervalMs)
                    }
                } else {
                    Log.e(tag, "Error: App is tracked but getAppId returned -1 for $packageName")
                }
            }
        }
    }

    /**
     * Checks if the given package has exceeded its limit.
     * If yes, launches InterruptionActivity.
     * @return true if blocked, false otherwise
     */
    private fun checkAndBlockIfLimitReached(packageName: String): Boolean {
        // 1. Refresh usage stats
        // Note: isAppBlockedNow calculates the open session duration dynamically (now - start_time),
        // so we don't need to manually update the database record here.

        val isBlocked = dbHelper.isAppBlockedNow(packageName)
        
        if (isBlocked) {
            Log.i(tag, "Limit reached for $packageName. Launching interruption.")

            val blockIntent = Intent(this, InterruptionActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putExtra("BLOCKED_PACKAGE", packageName)
            }
            startActivity(blockIntent)
            return true
        }
        return false
    }

    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    override fun onInterrupt() {
        Log.w(tag, "Accessibility service interrupted.")
        cleanup()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "onDestroy: Accessibility Service is being destroyed.")
        unregisterReceiver(closeAppReceiver)
        cleanup()
    }

    private fun cleanup() {
        monitorHandler.removeCallbacks(monitorRunnable)
        endCurrentSession()
    }

    private fun endCurrentSession() {
        currentForegroundApp?.let { app ->
            val appId = dbHelper.getAppId(app)
            if (appId != -1) {
                dbHelper.endUsageSession(appId)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(tag, "BlockingAccessibilityService connected.")
    }
}
