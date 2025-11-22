package com.latergator.features.interrupts.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import com.latergator.features.interrupts.InterruptionActivity
import com.latergator.data.DatabaseHelper

class BlockingAccessibilityService : AccessibilityService() {

    private val TAG = "BlockingService"
    private lateinit var dbHelper: DatabaseHelper
    private var currentForegroundApp: String? = null

    private val closeAppReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Accessibility Service is being created.")
        dbHelper = DatabaseHelper(this)
        registerReceiver(closeAppReceiver, IntentFilter(InterruptionActivity.ACTION_CLOSE_APP), Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.v(TAG, "onAccessibilityEvent received: ${event?.toString()}") // Verbose: log every event

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            Log.d(TAG, "Window state changed for package: $packageName")

            if (packageName == null || packageName == currentForegroundApp) {
                Log.d(TAG, "Package is null or same as current app ($currentForegroundApp). Ignoring.")
                return
            }

            Log.i(TAG, "App change detected. Previous: $currentForegroundApp, New: $packageName")

            // End session for the PREVIOUS app
            currentForegroundApp?.let { oldApp ->
                val oldAppId = dbHelper.getAppId(oldApp)
                if (oldAppId != -1) {
                    dbHelper.endUsageSession(oldAppId)
                    Log.i(TAG, "ENDED usage session for $oldApp (appId: $oldAppId)")
                } else {
                    Log.w(TAG, "Could not find appId for previous app: $oldApp")
                }
            }

            // Update current foreground app
            currentForegroundApp = packageName

            // --- Handle NEW app ---
            if (packageName == this.packageName) {
                Log.d(TAG, "New app is LaterGator itself. Ignoring.")
                return
            }

            if (isLauncher(packageName)) {
                Log.d(TAG, "New app is the launcher. Ignoring.")
                return
            }

            val trackedApps = dbHelper.getTrackedApps()
            Log.d(TAG, "Currently tracked apps: ${trackedApps.keys.joinToString()}")

            if (trackedApps.containsKey(packageName)) {
                Log.i(TAG, "$packageName is a tracked app.")
                val newAppId = dbHelper.getAppId(packageName)
                if (newAppId != -1) {
                    dbHelper.incrementUsage(newAppId)
                    Log.i(TAG, "STARTED usage session for $packageName (appId: $newAppId)")
                } else {
                    Log.e(TAG, "Failed to get appId for tracked app: $packageName")
                }
            } else {
                Log.d(TAG, "$packageName is NOT a tracked app.")
            }

            // Check if the new app should be blocked
            val isBlocked = dbHelper.isAppBlockedNow(packageName)
            if (isBlocked) {
                Log.w(TAG, "App is currently BLOCKED: $packageName. Launching interruption.")

                val blockIntent = Intent(this, InterruptionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("BLOCKED_PACKAGE", packageName)
                }
                startActivity(blockIntent)
            }
        }
    }

    private fun isLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo?.activityInfo?.packageName == packageName
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted.")
        endCurrentSession()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "onDestroy: Accessibility Service is being destroyed.")
        unregisterReceiver(closeAppReceiver)
        endCurrentSession()
    }

    private fun endCurrentSession() {
        currentForegroundApp?.let { app ->
            val appId = dbHelper.getAppId(app)
            if (appId != -1) {
                dbHelper.endUsageSession(appId)
                Log.d(TAG, "Ended session for $app due to service interruption/destruction.")
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "BlockingAccessibilityService connected.")
    }
}
