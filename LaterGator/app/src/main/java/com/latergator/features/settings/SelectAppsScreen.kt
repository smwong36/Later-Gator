package com.latergator.features.settings

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.latergator.R
import com.latergator.data.DatabaseHelper
import com.latergator.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SelectAppsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    val scope = rememberCoroutineScope()

    var trackedApps by remember { mutableStateOf<Map<String, Int?>>(emptyMap()) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            installedApps = getInstalledApps(context)
            trackedApps = dbHelper.getTrackedApps()
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.select_apps_screen_title))

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(installedApps, key = { it.packageName }) { app ->
                    AppRow(appInfo = app, isChecked = app.packageName in trackedApps.keys) { isChecked ->
                        scope.launch {
                            trackedApps = if (isChecked) {
                                trackedApps + (app.packageName to null) // Default to No Limit
                            } else {
                                trackedApps - app.packageName
                            }

                            withContext(Dispatchers.IO) {
                                updateTrackingStatus(dbHelper, app.packageName, isChecked)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text(stringResource(R.string.done))
        }
    }
}

private fun updateTrackingStatus(dbHelper: DatabaseHelper, packageName: String, isTracked: Boolean) {
    val db = dbHelper.writableDatabase
    try {
        db.beginTransaction()

        // Update the is_tracked status in the 'apps' table
        val appValues = ContentValues().apply {
            put("is_tracked", if (isTracked) 1 else 0)
            put("updated_at_ms", System.currentTimeMillis())
        }
        db.update("apps", appValues, "package_name = ?", arrayOf(packageName))

        // Find the app_id for the given package name
        val cursor = db.rawQuery("SELECT id FROM apps WHERE package_name = ?", arrayOf(packageName))
        var appId: Long = -1
        if (cursor.moveToFirst()) {
            val appIdIndex = cursor.getColumnIndex("id")
            if (appIdIndex != -1) {
                appId = cursor.getLong(appIdIndex)
            }
        }
        cursor.close()

        // If we found an app_id, update the 'active' status in 'time_limit_prefs'
        if (appId != -1L) {
            val prefValues = ContentValues().apply {
                put("active", if (isTracked) 1 else 0)
                put("updated_at_ms", System.currentTimeMillis())
            }
            db.update("time_limit_prefs", prefValues, "app_id = ?", arrayOf(appId.toString()))
        }
        
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}

@Composable
private fun AppRow(appInfo: AppInfo, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = appInfo.icon),
            contentDescription = "${appInfo.name} icon",
            modifier = Modifier.size(40.dp)
        )
        Text(text = appInfo.name, modifier = Modifier.weight(1f))
        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

private fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val allApps = pm.queryIntentActivities(intent, 0)
    val dbHelper = DatabaseHelper(context)
    val db = dbHelper.writableDatabase

    try {
        db.beginTransaction()
        for (resolveInfo in allApps) {
            val appName = resolveInfo.loadLabel(pm).toString()
            val packageName = resolveInfo.activityInfo.packageName

            val values = ContentValues().apply {
                put("package_name", packageName)
                put("label", appName)
                put("is_tracked", 0) // Default to not selected
                put("created_at_ms", System.currentTimeMillis())
                put("updated_at_ms", System.currentTimeMillis())
            }
            db.insertWithOnConflict("apps", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }

    return allApps.mapNotNull { resolveInfo ->
        val packageName = resolveInfo.activityInfo.packageName
        if (packageName != context.packageName) {
            AppInfo(
                name = resolveInfo.loadLabel(pm).toString(),
                packageName = packageName,
                icon = resolveInfo.loadIcon(pm)
            )
        } else {
            null
        }
    }.sortedBy { it.name.lowercase() }
}
