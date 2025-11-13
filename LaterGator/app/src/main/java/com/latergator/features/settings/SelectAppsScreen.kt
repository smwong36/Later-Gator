package com.latergator.features.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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

    var trackedApps by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val db = dbHelper.readableDatabase
            val cursor = db.rawQuery("SELECT package_name FROM apps", null)
            val packages = mutableMapOf<String, Int>()
            if (cursor.moveToFirst()) {
                do {
                    val pkg = cursor.getString(0)
                    packages[pkg] = 1
                } while (cursor.moveToNext())
            }
            cursor.close()
            trackedApps = packages
            installedApps = getInstalledApps(context)
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
                        scope.launch(Dispatchers.IO) {
                            if (isChecked) {
                                val db = dbHelper.writableDatabase
                                db.execSQL("INSERT OR IGNORE INTO apps (package_name, created_at_ms) VALUES (?, ?)", arrayOf(app.packageName, System.currentTimeMillis()))
                            } else {
                                val db = dbHelper.writableDatabase
                                db.execSQL("DELETE FROM apps WHERE package_name = ?", arrayOf(app.packageName))
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
    return allApps.mapNotNull {
        val appName = it.loadLabel(pm).toString()
        val packageName = it.activityInfo.packageName
        if (packageName != context.packageName) {
            AppInfo(appName, packageName, it.loadIcon(pm))
        } else {
            null
        }
    }.sortedBy { it.name.lowercase() }
}
