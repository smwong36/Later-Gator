package com.latergator.features.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.latergator.R
import com.latergator.data.AppConfig
import com.latergator.data.DatabaseHelper
import com.latergator.features.interrupts.accessibility.BlockingAccessibilityService
import com.latergator.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
// Data classes for tracking apps and their configurations
data class TrackedApp(val appInfo: AppInfo, val config: AppConfig)
// Settings screen for the app
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // --- State Management ---
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    
    var trackedApps by remember { mutableStateOf<List<TrackedApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }

    // --- Lifecycle & Data Loading ---
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            userName = dbHelper.getUserProfileName() ?: ""
        }
    }
    // Observe lifecycle to update permissions
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsagePermission = hasUsageStatsPermission(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
                hasOverlayPermission = Settings.canDrawOverlays(context)
                
                scope.launch {
                    isLoading = true
                    val apps = withContext(Dispatchers.IO) {
                        // Now fetches the full AppConfig (time limit + snoozes)
                        val configs = dbHelper.getTrackedAppsConfig()
                        getTrackedAppDetails(context, configs)
                    }
                    trackedApps = apps
                    isLoading = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // --- Permission Handling ---
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { 
        hasUsagePermission = hasUsageStatsPermission(context)
        hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
        hasOverlayPermission = Settings.canDrawOverlays(context)
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_screen_title),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        // --- Profile Settings ---
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    val wasSuccessful = withContext(Dispatchers.IO) {
                        dbHelper.updateUserProfileName(userName)
                    }
                    val message = if (wasSuccessful) {
                        "Username updated!"
                    } else {
                        "Username cannot be blank."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                focusManager.clearFocus()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Update")
        }

        Spacer(Modifier.height(24.dp))

        // --- Permissions Section ---
        PermissionStatusRow(
            label = stringResource(R.string.usage_permission_label),
            hasPermission = hasUsagePermission,
            onClick = { settingsLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }
        )

        Spacer(Modifier.height(16.dp))

        PermissionStatusRow(
            label = "Accessibility Permission",
            hasPermission = hasAccessibilityPermission,
            onClick = { settingsLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        )

        Spacer(Modifier.height(16.dp))

        // Grant Overlay Permission (Draw over other apps)
        PermissionStatusRow(
            label = "Display Over Other Apps",
            hasPermission = hasOverlayPermission,
            onClick = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            }
        )

        Spacer(Modifier.height(32.dp))

        // --- Tracked Apps Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.tracked_apps_title))
            Button(onClick = { navController.navigate("select_apps") }) {
                Text(stringResource(R.string.manage_apps_button))
            }
        }

        // Explanation Text
        Text(
            text = "Each snooze adds 5 minutes of app time.",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(Modifier.height(8.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(trackedApps, key = { it.appInfo.packageName }) { app ->
                    TrackedAppRow(
                        trackedApp = app,
                        onTimeLimitChange = {
                            scope.launch(Dispatchers.IO) {
                                dbHelper.updateTimeLimit(app.appInfo.packageName, it)
                            }
                        },
                        onTimeLimitChangeFinished = { newLimit ->
                            // Optimistically update the list
                            val newTrackedApps = trackedApps.map {
                                if (it.appInfo.packageName == app.appInfo.packageName) {
                                    it.copy(config = it.config.copy(timeLimit = newLimit))
                                } else {
                                    it
                                }
                            }
                            trackedApps = newTrackedApps
                        },
                        onSnoozeLimitChange = { daily, weekly ->
                             scope.launch(Dispatchers.IO) {
                                dbHelper.updateSnoozeLimit(app.appInfo.packageName, daily, weekly)
                            }
                            // Optimistically update the list
                            val newTrackedApps = trackedApps.map {
                                if (it.appInfo.packageName == app.appInfo.packageName) {
                                    it.copy(config = it.config.copy(dailySnoozeLimit = daily, weeklySnoozeLimit = weekly))
                                } else {
                                    it
                                }
                            }
                            trackedApps = newTrackedApps
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.back_to_home))
        }
    }
}
// Helper composable for displaying permission status
@Composable
fun PermissionStatusRow(label: String, hasPermission: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        val statusText = if (hasPermission) "Granted" else "Not Granted"
        Text(text = "Status: $statusText")
    }
    if (!hasPermission) {
        Spacer(Modifier.height(8.dp))
        Button(onClick = onClick) {
            Text("Grant")
        }
    }
}
// Tracked App Row and Settings
@Composable
private fun TrackedAppRow(
    trackedApp: TrackedApp,
    onTimeLimitChange: (Int?) -> Unit,
    onTimeLimitChangeFinished: (Int?) -> Unit,
    onSnoozeLimitChange: (daily: Int, weekly: Int) -> Unit
) {
    // We use -1f to represent "No limit" for the slider's internal state
    var sliderPosition by remember { mutableStateOf(trackedApp.config.timeLimit?.toFloat() ?: -1f) }
    
    // Determine if snooze settings should be enabled
    // Enabled only if there is a valid time limit (slider >= 0)
    val isSnoozeEnabled = sliderPosition >= 0
    // UI layout
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Top Row: Icon, Name, Time Limit Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = trackedApp.appInfo.icon),
                contentDescription = "${trackedApp.appInfo.name} icon",
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trackedApp.appInfo.name, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = -1f..180f, // -1 is our special value for "No limit"
                    onValueChangeFinished = {
                        val newLimit = if (sliderPosition < 0) null else sliderPosition.toInt()
                        onTimeLimitChange(newLimit)
                        onTimeLimitChangeFinished(newLimit)
                    }
                )
            }
            val limitText = when (val limit = if (sliderPosition < 0) null else sliderPosition.toInt()) {
                null -> "No limit"
                0 -> "0 min"
                else -> "$limit min"
            }
            Text(limitText, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Row: Snooze Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SnoozeStepper(
                label = "Daily Snoozes",
                value = trackedApp.config.dailySnoozeLimit,
                max = trackedApp.config.weeklySnoozeLimit, // Constraint: Daily cannot exceed Weekly
                enabled = isSnoozeEnabled, // Pass enabled state
                onValueChange = { newValue ->
                    onSnoozeLimitChange(newValue, trackedApp.config.weeklySnoozeLimit)
                }
            )
            
            SnoozeStepper(
                label = "Weekly Snoozes",
                value = trackedApp.config.weeklySnoozeLimit,
                max = 10,
                enabled = isSnoozeEnabled, // Pass enabled state
                onValueChange = { newValue ->
                    // Logic: If weekly limit drops below daily limit, clamp daily limit
                    val validDaily = if (trackedApp.config.dailySnoozeLimit > newValue) {
                        newValue 
                    } else {
                        trackedApp.config.dailySnoozeLimit
                    }
                    onSnoozeLimitChange(validDaily, newValue)
                }
            )
        }
    }
}
// Stepper for snoozes
@Composable
private fun SnoozeStepper(
    label: String,
    value: Int,
    max: Int = 10, 
    enabled: Boolean = true, // Added enabled parameter
    onValueChange: (Int) -> Unit
) {
    // Reduce opacity if disabled
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (enabled) 1f else 0.38f) 
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                modifier = Modifier.size(32.dp),
                enabled = enabled && value > 0 // Check enabled state
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            IconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                modifier = Modifier.size(32.dp),
                enabled = enabled && value < max // Check enabled state
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}
// Helper functions for permissions
//Checks whether the app has permission to access Usage Stats , the app time
private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}
// Checks whether the app has accessibility service enabled
private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/${BlockingAccessibilityService::class.java.canonicalName}"
    try {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        val stringColonSplitter = TextUtils.SimpleStringSplitter(':')
        stringColonSplitter.setString(enabledServices)
        while (stringColonSplitter.hasNext()) {
            val componentName = stringColonSplitter.next()
            if (componentName.equals(service, ignoreCase = true)) {
                return true
            }
        }
    } catch (e: Exception) {
        // Silently fail
    }
    return false
}
// gets tracked app details
private fun getTrackedAppDetails(context: Context, configs: List<AppConfig>): List<TrackedApp> {
    val pm = context.packageManager
    // Map the AppConfig list to TrackedApp objects
    return configs.mapNotNull { config ->
        try {
            val appInfo = pm.getApplicationInfo(config.packageName, 0)
            TrackedApp(
                appInfo = AppInfo(
                    name = appInfo.loadLabel(pm).toString(),
                    packageName = config.packageName,
                    icon = appInfo.loadIcon(pm)
                ),
                config = config
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }.sortedBy { it.appInfo.name.lowercase() }
}
