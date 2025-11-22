package com.latergator.features.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.latergator.R
import com.latergator.data.DatabaseHelper
import com.latergator.features.interrupts.accessibility.BlockingAccessibilityService
import com.latergator.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TrackedApp(val appInfo: AppInfo, val timeLimitMinutes: Int?)

@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // --- State Management ---
    var hasUsagePermission by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var hasAccessibilityPermission by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var trackedApps by remember { mutableStateOf<List<TrackedApp>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }

    // --- Lifecycle & Data Loading ---
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            userName = dbHelper.getUserProfileName() ?: ""
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsagePermission = hasUsageStatsPermission(context)
                hasAccessibilityPermission = isAccessibilityServiceEnabled(context)
                scope.launch {
                    isLoading = true
                    val apps = withContext(Dispatchers.IO) {
                        val trackedPackages = dbHelper.getTrackedApps()
                        getTrackedAppDetails(context, trackedPackages)
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
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = android.net.Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant Overlay Permission")
        }

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
                            val newTrackedApps = trackedApps.map {
                                if (it.appInfo.packageName == app.appInfo.packageName) {
                                    it.copy(timeLimitMinutes = newLimit)
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

@Composable
private fun TrackedAppRow(
    trackedApp: TrackedApp,
    onTimeLimitChange: (Int?) -> Unit,
    onTimeLimitChangeFinished: (Int?) -> Unit
) {
    // We use -1f to represent "No limit" for the slider's internal state
    var sliderPosition by remember { mutableStateOf(trackedApp.timeLimitMinutes?.toFloat() ?: -1f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = trackedApp.appInfo.icon),
            contentDescription = "${trackedApp.appInfo.name} icon",
            modifier = Modifier.size(40.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = trackedApp.appInfo.name)
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
        Text(limitText)
    }
}

private fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

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

private fun getTrackedAppDetails(context: Context, trackedPackages: Map<String, Int?>): List<TrackedApp> {
    val pm = context.packageManager
    return trackedPackages.mapNotNull { (packageName, timeLimit) ->
        try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            TrackedApp(
                appInfo = AppInfo(
                    name = appInfo.loadLabel(pm).toString(),
                    packageName = packageName,
                    icon = appInfo.loadIcon(pm)
                ),
                timeLimitMinutes = timeLimit
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }.sortedBy { it.appInfo.name.lowercase() }
}
