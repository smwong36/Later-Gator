package com.latergator.data

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Calendar

// Holds all configuration for a tracked app
data class AppConfig(
    val packageName: String,
    val timeLimit: Int?, // Daily time limit in minutes
    val dailySnoozeLimit: Int, // Max snoozes per day
    val weeklySnoozeLimit: Int // Max snoozes per week
)
/* Holds all configuration for a tracked app
 - whether it is currently blocked or not
 - how much time has been used today
 - the daily time limit
 */
data class AppUsageStatus(
    val appId: Int,
    val isBlocked: Boolean,
    val usedMinutes: Int,
    val limitMinutes: Int
)
/*
    Database helper is responsible for creating and managing the SQLite database
    and tables
 */
class DatabaseHelper(private val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "latergator.db"
        private const val DB_VERSION = 1
        private const val DB_PATH_SUFFIX = "/databases/"
    }

    init {
        createDatabaseIfNotExists()
    }
    // Returns the path to the database file
    private fun dbPath(): String {
        return context.applicationInfo.dataDir + DB_PATH_SUFFIX + DB_NAME
    }
    // Creates the database if it doesn't exist
    private fun createDatabaseIfNotExists() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            // We need to close the readable database immediately after creating the file structure
            // so we can overwrite it with the asset copy.
            this.readableDatabase.close() 
            copyDatabaseFromAssets()
        }
    }
    // Copies the database from assets to the application's database directory
    private fun copyDatabaseFromAssets() {
        try {
            val dbPath = context.getDatabasePath(DB_NAME)
            dbPath.parentFile?.mkdirs() // Ensure the database directory exists

            val inputStream: InputStream = context.assets.open(DB_NAME)
            val outFileName = dbPath.path
            val outputStream: OutputStream = FileOutputStream(outFileName)

            val buffer = ByteArray(1024)
            var length: Int
            while (true) {
                length = inputStream.read(buffer)
                if (length <= 0) break
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
            throw RuntimeException("Error copying database from assets", e)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Do nothing. Schema already exists in prebuilt DB.
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle DB upgrades if needed
    }
    // Helper function to check if the profile exists in the database and return true if it does
    fun hasProfile(): Boolean {
        val db = readableDatabase
        return db.rawQuery("SELECT id FROM profile LIMIT 1", null).use { cursor ->
            cursor.moveToFirst()
        }
    }
    // Helper function to create a new user profile and insert it into the database
    fun createUserProfile(userName: String, tz: String) {
        if (userName.isBlank()) {
            return // Do not allow blank names
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put("user_name", userName)
                put("tz", tz)
                put("created_at_ms", now)
                put("updated_at_ms", now)
                put("privacy_level", "local_only") // Set default for NOT NULL column
            }
            db.insert("profile", null, values)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun updateUserProfileName(newName: String): Boolean {
        if (newName.isBlank()) {
            return false// Do not allow blank names
        }
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_name", newName)
            put("updated_at_ms", System.currentTimeMillis())
        }
        return db.update("profile", values, null, null) > 0
    }

    fun getUserProfileName(): String? {
        val db = readableDatabase
        return db.rawQuery("SELECT user_name FROM profile LIMIT 1", null).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                null
            }
        }
    }

    // Deprecated: Use getTrackedAppsConfig instead for more details
    fun getTrackedApps(): Map<String, Int?> {
        return getTrackedAppsConfig().associate { it.packageName to it.timeLimit }
    }

    fun getTrackedAppsConfig(): List<AppConfig> {
        val db = readableDatabase
        val appConfigs = mutableListOf<AppConfig>()
        
        // Join apps with both preference tables
        // We assume scope = 'per_app' for both settings
        val query = """
            SELECT 
                a.package_name, 
                t.daily_limit_minutes_current,
                s.max_per_day_current,
                s.max_per_week_current
            FROM apps a
            LEFT JOIN time_limit_prefs t ON a.id = t.app_id AND t.scope = 'per_app' AND t.active = 1
            LEFT JOIN snooze_prefs s ON a.id = s.app_id AND s.scope = 'per_app'
            WHERE a.is_tracked = 1
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        try {
            if (cursor.moveToFirst()) {
                do {
                    val packageNameIndex = cursor.getColumnIndex("package_name")
                    val timeLimitIndex = cursor.getColumnIndex("daily_limit_minutes_current")
                    val dailySnoozeIndex = cursor.getColumnIndex("max_per_day_current")
                    val weeklySnoozeIndex = cursor.getColumnIndex("max_per_week_current")

                    if (packageNameIndex != -1) {
                        val packageName = cursor.getString(packageNameIndex)
                        
                        val timeLimit = if (timeLimitIndex != -1 && !cursor.isNull(timeLimitIndex)) {
                            cursor.getInt(timeLimitIndex)
                        } else {
                            null
                        }
                        
                        val dailySnooze = if (dailySnoozeIndex != -1 && !cursor.isNull(dailySnoozeIndex)) {
                            cursor.getInt(dailySnoozeIndex)
                        } else {
                            0 // Default to 0
                        }

                        val weeklySnooze = if (weeklySnoozeIndex != -1 && !cursor.isNull(weeklySnoozeIndex)) {
                            cursor.getInt(weeklySnoozeIndex)
                        } else {
                            0 // Default to 0
                        }
                        
                        appConfigs.add(AppConfig(packageName, timeLimit, dailySnooze, weeklySnooze))
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }
        return appConfigs
    }
    // Helper function to get the app's ID from the database
    fun getAppId(packageName: String): Int {
        val db = readableDatabase
        return db.rawQuery("SELECT id FROM apps WHERE package_name = ?", arrayOf(packageName)).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getInt(0)
            } else {
                -1
            }
        }
    }

    /*fun setAppTrackingAndActiveStatus(packageName: String, isTracked: Boolean) {
        val db = writableDatabase
        db.beginTransaction()
        try {
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
*/
    fun updateTimeLimit(packageName: String, newLimit: Int?) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery("SELECT id FROM apps WHERE package_name = ?", arrayOf(packageName))
            var appId: Long = -1
            if (cursor.moveToFirst()) {
                val appIdIndex = cursor.getColumnIndex("id")
                if (appIdIndex != -1) {
                    appId = cursor.getLong(appIdIndex)
                }
            }
            cursor.close()

            if (appId == -1L) return

            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                if (newLimit == null) {
                    putNull("daily_limit_minutes_current")
                    putNull("daily_limit_minutes_original")
                } else {
                    put("daily_limit_minutes_current", newLimit)
                    put("daily_limit_minutes_original", newLimit)
                }
                put("updated_at_ms", now)
            }

            val updatedRows = db.update(
                "time_limit_prefs",
                values,
                "app_id = ? AND scope = ?",
                arrayOf(appId.toString(), "per_app")
            )

            if (updatedRows == 0 && newLimit != null) {
                values.put("scope", "per_app")
                values.put("app_id", appId)
                values.put("active", 1)
                values.put("created_at_ms", now)
                // daily_limit_minutes_original already set in apply block
                db.insertWithOnConflict("time_limit_prefs", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    // Helper function to update the snooze limit for a given app
    fun updateSnoozeLimit(packageName: String, dailyLimit: Int, weeklyLimit: Int) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery("SELECT id FROM apps WHERE package_name = ?", arrayOf(packageName))
            var appId: Long = -1
            if (cursor.moveToFirst()) {
                val appIdIndex = cursor.getColumnIndex("id")
                if (appIdIndex != -1) {
                    appId = cursor.getLong(appIdIndex)
                }
            }
            cursor.close()

            if (appId == -1L) return

            val now = System.currentTimeMillis()
            val values = ContentValues().apply {
                put("max_per_day_current", dailyLimit)
                put("max_per_week_current", weeklyLimit)
                put("updated_at_ms", now)
            }

            val updatedRows = db.update(
                "snooze_prefs",
                values,
                "app_id = ? AND scope = ?",
                arrayOf(appId.toString(), "per_app")
            )
            // If no rows were updated, insert a new row
            if (updatedRows == 0) {
                // Need to insert new row. Must provide all NOT NULL columns.
                values.put("app_id", appId)
                values.put("scope", "per_app")
                // Set originals to match current for a fresh insert
                values.put("max_per_day_original", dailyLimit)
                values.put("max_per_week_original", weeklyLimit)
                // Removed daily_snoozes_remaining based on schema
                // Removed duration_minutes as the column doesn't exist
                values.put("created_at_ms", now)
                
                db.insertWithOnConflict("snooze_prefs", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
    // Helper function to set the tracking status for an app
    fun setAppTrackingAndActiveStatus(packageName: String, isTracked: Boolean) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()
            
            // Update the is_tracked status in the 'apps' table
            val appValues = ContentValues().apply {
                put("is_tracked", if (isTracked) 1 else 0)
                put("updated_at_ms", now)
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
                // 1. Update time_limit_prefs
                val prefValues = ContentValues().apply {
                    put("active", if (isTracked) 1 else 0)
                    put("updated_at_ms", now)
                }
                db.update("time_limit_prefs", prefValues, "app_id = ?", arrayOf(appId.toString()))

                // 2. Ensure snooze_prefs row exists if tracking is enabled
                if (isTracked) {
                    // Check if a snooze preference already exists
                    val snoozeCheck = db.rawQuery(
                        "SELECT id FROM snooze_prefs WHERE app_id = ? AND scope = 'per_app'", 
                        arrayOf(appId.toString())
                    )
                    val hasSnoozePrefs = snoozeCheck.moveToFirst()
                    snoozeCheck.close()

                    if (!hasSnoozePrefs) {
                        // Create default snooze prefs (0 daily, 0 weekly)
                        val snoozeValues = ContentValues().apply {
                            put("app_id", appId)
                            put("scope", "per_app")
                            put("max_per_day_current", 0)
                            put("max_per_week_current", 0)
                            put("max_per_day_original", 0)
                            put("max_per_week_original", 0)
                            // Removed daily_snoozes_remaining
                            // Removed duration_minutes
                            put("created_at_ms", now)
                            put("updated_at_ms", now)
                        }
                        db.insert("snooze_prefs", null, snoozeValues)
                    }
                }
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** ==================================
     * EVENT LOG /  USAGE HANDLERS
     * ==================================
     */
    /**
     * logEvent()
     * @param eventType A string describing the type of event (e.g., "snooze_used", "limit_exit").
     * @param metadata Optional string for any additional data about the event.
     * @param appId Optional ID of the app associated with the event.
     */
    fun logEvent(eventType: String, metadata: String? = null, appId: Int? = null) {
        val db = writableDatabase
        val timestamp = System.currentTimeMillis()

        // Escape metadata if null
        val metadataEscaped = metadata?.replace("'", "''")
        val appIdVal = appId?.toString() ?: "NULL"
        val metaVal = metadataEscaped?.let { "'$it'" } ?: "NULL"

        val insertQuery = """
        INSERT INTO events (kind, meta_json, at_ms, app_id)
        VALUES ('$eventType', $metaVal, $timestamp, $appIdVal)
    """.trimIndent()

        db.execSQL(insertQuery)
    }

    /**
     * logLimitExitEvent()
     * Records a session and logs an event when the user exits an app due to hitting a time limit.
     *
     * This writes to both `usage_sessions` and `events` tables.
     *
     * @param appId ID of the app the user exited from.
     * @param endedAt End timestamp (ms) of the session.
     * @param source Optional string identifying how the exit was triggered (e.g., 'popup', 'system').
     */
    fun logLimitExitEvent(appId: Int, endedAt: Long, source: String = "system") {
        val db = writableDatabase
        var startedAt: Long = -1
        var sessionId: Long = -1

        // Changed IS NULL to = 0 to match Option B requirement
        db.rawQuery("SELECT id, started_at_ms FROM usage_sessions WHERE app_id = ? AND ended_at_ms = 0 ORDER BY started_at_ms DESC LIMIT 1", arrayOf(appId.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                sessionId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                startedAt = cursor.getLong(cursor.getColumnIndexOrThrow("started_at_ms"))
            }
        }

        if (sessionId != -1L) {
            val duration = endedAt - startedAt
            val values = ContentValues().apply {
                put("ended_at_ms", endedAt)
                put("duration_ms", duration)
                put("source", source)
                put("blocked_flag", 1)  // Marked as blocked due to time limit
            }
            db.update("usage_sessions", values, "id = ?", arrayOf(sessionId.toString()))
        }

        // Log the limit exit event in the events table
        logEvent("limit_exit", appId = appId)
    }

    /** ==================================
     * INTERRUPTION / POPUP HANDLERS
     * ==================================
     */

    /**
     * incrementUsage()
     * Updates the usage_sessions table when an app is detected in the foreground.
     *
     * - If there's already an ongoing session (ended_at_ms = 0), we don't start a new one.
     * - If no session exists for this app today, or the last session has ended, we insert a new session.
     * 
     * This method assumes it is called periodically (e.g., every 10 seconds)
     * when the app is confirmed to be in the foreground.
     *
     * @param appId The app_id from the apps table that is currently active
     */
    fun incrementUsage(appId: Int) {
        val startOfDay = getStartOfTodayInMillis()
        val db = writableDatabase
        val now = System.currentTimeMillis()

        // Changed IS NULL to = 0
        var hasOpenSession = false
        db.rawQuery(
            """
            SELECT id FROM usage_sessions
            WHERE app_id = ? AND started_at_ms >= ? AND ended_at_ms = 0
            ORDER BY started_at_ms DESC LIMIT 1
            """ .trimIndent(),
            arrayOf(appId.toString(), startOfDay.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                hasOpenSession = true
            }
        }

        // If no open session, insert a new one
        if (!hasOpenSession) {
            val values = ContentValues().apply {
                put("app_id", appId)
                put("started_at_ms", now)
                put("ended_at_ms", 0) // OPTION B: Default value for NOT NULL constraint
                put("duration_ms", 0) // OPTION B: Default value for NOT NULL constraint
                put("source", "system") // Default for NOT NULL constraint
                put("blocked_flag", 0)  // Default for NOT NULL constraint
            }
            db.insert("usage_sessions", null, values)
        }
    }

    /**
     * endUsageSession()
     * Updates the usage_sessions table to close an ongoing session for an app.
     *
     * - Finds the latest open session (ended_at_ms = 0) for the given app.
     * - Sets the ended_at_ms to the current time and calculates the duration.
     *
     * @param appId The app_id from the apps table that is no longer active.
     */
    fun endUsageSession(appId: Int) {
        val db = writableDatabase
        val now = System.currentTimeMillis()
        var sessionId = -1L
        var startedAt = -1L

        // Changed IS NULL to = 0
        db.rawQuery(
            "SELECT id, started_at_ms FROM usage_sessions WHERE app_id = ? AND ended_at_ms = 0 ORDER BY started_at_ms DESC LIMIT 1",
            arrayOf(appId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                sessionId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                startedAt = cursor.getLong(cursor.getColumnIndexOrThrow("started_at_ms"))
            }
        }

        if (sessionId != -1L) {
            val duration = now - startedAt
            val values = ContentValues().apply {
                put("ended_at_ms", now)
                put("duration_ms", duration)
            }
            db.update("usage_sessions", values, "id = ?", arrayOf(sessionId.toString()))
        }
    }

    /**
     * resetDailyCounters()
     * Captures the current user-defined limits as the original values for the new day.
     * Also resets snooze counts in snooze_settings.
     */
    fun resetDailyCounters() {
        val db = writableDatabase
        // Copy user-defined limits into the original columns to freeze the day's values
        db.execSQL("""
            UPDATE time_limit_prefs 
            SET daily_limit_minutes_original = daily_limit_minutes_current
        """)

        // Removed update to snooze_settings as daily_snoozes_remaining column doesn't exist
    }

    /**
     * getAppTimeUsedToday()
     * Returns the total usage time in minutes for the given app for the current day.
     *
     * This uses the `usage_sessions` table and sums up all finished sessions
     * that began today at or after 00:00 local time.
     *
     * @param appId The app's unique identifier as stored in the DB.
     * @return Total number of minutes used today for this app.
     */
    fun getAppTimeUsedToday(appId: Int): Int {
        val db = readableDatabase
        val startOfDay = getStartOfTodayInMillis()
        val query = """
            SELECT SUM(duration_ms) as total_usage
            FROM usage_sessions
            WHERE app_id = ? AND started_at_ms >= ? AND ended_at_ms > 0
        """
        return db.rawQuery(query, arrayOf(appId.toString(), startOfDay.toString())).use { cursor ->
            var totalTimeMinutes = 0
            if (cursor.moveToFirst()) {
                val durationMs = cursor.getLong(cursor.getColumnIndexOrThrow("total_usage"))
                totalTimeMinutes = (durationMs / 60000).toInt()  // Convert ms to minutes
            }
            totalTimeMinutes
        }
    }

    // Helper function to get the start of today in milliseconds
    // Returns user-configurable start-time, if exists, otherwise defaults to midnight
    private fun getStartOfTodayInMillis(): Long {
        val calendar = Calendar.getInstance()

        // Try to get custom start hour
        val customStartHour = getUserStartTimeHour()

        calendar.set(Calendar.HOUR_OF_DAY, customStartHour ?: 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // If current time is before start time, back up to yesterday's start
        if (System.currentTimeMillis() < calendar.timeInMillis) {
            calendar.add(Calendar.DATE, -1)
        }

        return calendar.timeInMillis
    }

    // Fetches the user's start-of-day time from the mode_settings table, or null if none set
    private fun getUserStartTimeHour(): Int? {
        val db = readableDatabase
        return db.rawQuery(
            """
            SELECT start_time_local 
            FROM mode_settings 
            WHERE enabled = 1 AND start_time_local IS NOT NULL 
            ORDER BY priority DESC LIMIT 1
            """ .trimIndent(), null
        ).use { cursor ->
            var hour: Int? = null
            if (cursor.moveToFirst()) {
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time_local"))
                try {
                    // Expecting "HH:mm" format
                    val parts = startTime.split(":")
                    hour = parts[0].toInt()
                } catch (e: Exception) {
                    // If format is wrong or something crashes, just default later
                    hour = null
                }
            }
            hour
        }
    }

    /**
     * getOriginalDailyLimitForApp()
     * Retrieves the set daily usage limit in minutes for the given app.
     *
     * This checks the `time_limit_prefs` table using the appId and the 'per_app' scope.
     * If no limit is set (null), returns null to indicate "no limit".
     *
     * @param appId The app's unique identifier from the `apps` table.
     * @return Daily usage limit in minutes, or null if no limit is set.
     */
    fun getOriginalDailyLimitForApp(appId: Int): Int? {
        val db = readableDatabase
        val query = """
            SELECT daily_limit_minutes_original
            FROM time_limit_prefs
            WHERE app_id = ? AND scope = 'per_app' AND active = 1
            LIMIT 1
            """.trimIndent()

        return db.rawQuery(query, arrayOf(appId.toString())).use { cursor ->
            var limit: Int? = null
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    limit = cursor.getInt(0)
                }
            }
            limit
        }
    }

    /**
     * getGlobalDailyTimeUsedToday()
     * Returns the total number of minutes used today across all apps
     * currently being tracked (where is_tracked = 1).
     *
     * Used to enforce a global daily time limit across all selected apps.
     */
    fun getGlobalDailyTimeUsedToday(): Int {
        val db = readableDatabase
        val startOfDay = getStartOfTodayInMillis()

        val query = """
            SELECT SUM(duration_ms) 
            FROM usage_sessions
            WHERE app_id IN (
                SELECT id FROM apps WHERE is_tracked = 1
            ) AND started_at_ms >= ?
        """.trimIndent()

        return db.rawQuery(query, arrayOf(startOfDay.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                val totalMs = cursor.getLong(0)
                (totalMs / 60000).toInt()  // Convert ms → minutes
            } else {
                0
            }
        }
    }

    /**
     * getOriginalGlobalDailyLimit()
     * Returns the original global daily limit in minutes, if set.
     *
     * Reads from the time_limit_prefs table where scope = 'global'.
     * Returns null if no global daily cap was configured.
     */
    fun getOriginalGlobalDailyLimit(): Int? {
        val db = readableDatabase
        val query = """
            SELECT daily_limit_minutes_original
            FROM time_limit_prefs
            WHERE scope = 'global'
            LIMIT 1
            """.trimIndent()

        return db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) cursor.getInt(0) else null
            } else {
                null
            }
        }
    }

    /**
     * Checks if there is an active snooze for the given app.
     * Returns true if now < (latest_snooze_time + duration).
     */
    private fun isSnoozeActive(appId: Int): Boolean {
        val db = readableDatabase
        val now = System.currentTimeMillis()
        
        // 1. Get latest snooze timestamp for this app
        var lastSnoozeTime: Long = 0
        db.rawQuery(
            "SELECT used_at_ms FROM snooze_ledger WHERE app_id = ? AND reason = 'per_app' ORDER BY used_at_ms DESC LIMIT 1", 
            arrayOf(appId.toString())
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                lastSnoozeTime = cursor.getLong(0)
            }
        }

        if (lastSnoozeTime == 0L) return false

        // 2. Get duration preference
        // Since duration_minutes column does not exist, we default to 5 minutes
        var durationMinutes = 5 
        
        val snoozeEnd = lastSnoozeTime + (durationMinutes * 60_000L)
        return now < snoozeEnd
    }
    // Helper function to check if the profile exists in the database and return true if it does
    fun getAppUsageStatus(packageName: String): AppUsageStatus? {
        val db = readableDatabase
        // 1. Get App ID
        val appIdQuery = "SELECT id FROM apps WHERE package_name = ?"
        var appId: Int? = null
        db.rawQuery(appIdQuery, arrayOf(packageName)).use { appIdCursor ->
            if (appIdCursor.moveToFirst()) {
                appId = appIdCursor.getInt(0)
            }
        }
        
        if (appId == null) return null

        // Check if app is actively snoozed
        val isSnoozed = isSnoozeActive(appId)

        // 2. Check time limit
        // Calculate total usage today (Closed Sessions + Current Open Session)
        val startOfDay = getStartOfTodayInMillis()
        val now = System.currentTimeMillis()
        
        // Get Closed Sessions Duration
        // Changed IS NOT NULL to > 0
        val closedQuery = """
            SELECT SUM(duration_ms) 
            FROM usage_sessions
            WHERE app_id = ? AND started_at_ms >= ? AND ended_at_ms > 0
        """
        var totalMs = 0L
        db.rawQuery(closedQuery, arrayOf(appId.toString(), startOfDay.toString())).use { closedCursor ->
            if (closedCursor.moveToFirst()) {
                totalMs = closedCursor.getLong(0)
            }
        }
        
        // Get Current Open Session Duration
        // Changed IS NULL to = 0
        val openQuery = """
            SELECT started_at_ms 
            FROM usage_sessions
            WHERE app_id = ? AND started_at_ms >= ? AND ended_at_ms = 0
            LIMIT 1
        """
        db.rawQuery(openQuery, arrayOf(appId.toString(), startOfDay.toString())).use { openCursor ->
            if (openCursor.moveToFirst()) {
                val start = openCursor.getLong(0)
                totalMs += (now - start)
            }
        }

        val totalMinutes = (totalMs / 60000).toInt()

        // Get Limit
        val limit = getOriginalDailyLimitForApp(appId) ?: return AppUsageStatus(appId, false, totalMinutes, 0)
        
        // Blocked if over limit AND not snoozed
        val isBlocked = totalMinutes >= limit && !isSnoozed
        
        return AppUsageStatus(appId, isBlocked, totalMinutes, limit)
    }


    /**
     * logTimeLimitBreach()
     * Logs a time limit breach into the time_limit_hits_ledger table.
     * Called when an app exceeds its daily time limit.
     */
    fun logTimeLimitBreach(
        appId: Int,
        limitMinutes: Int,
        usedMinutes: Int,
        scope: String = "per_app",
        period: String = "daily",
        actionTaken: String = "blocked"
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("at_ms", System.currentTimeMillis())
            put("scope", scope)
            put("app_id", appId)
            put("period", period)
            put("limit_minutes", limitMinutes)
            put("used_minutes", usedMinutes)
            put("action_taken", actionTaken)
        }
        db.insert("time_limit_hits_ledger", null, values)
    }

    /** ==================================
     * SNOOZE HANDLERS
     * ==================================
     */

    /**
     * getRemainingSnoozes()
     * Returns how many snoozes are still available today for this app.
     * Scope is either "global" (all apps) or "per_app" (single app).
     *
     * @param scope "global" or "per_app"
     * @param appId ID of the app (ignored if scope is "global")
     * @return How many snoozes remain for today (0 or more)
     */
    fun getRemainingSnoozes(scope: String, appId: Int? = null): Int {
        val db = readableDatabase
        val todayStart = getStartOfTodayInMillis()

        val used = db.rawQuery(
            """
        SELECT COUNT(*) FROM snooze_ledger
        WHERE used_at_ms >= ? AND (? IS NULL OR app_id = ?)
        """ .trimIndent(),
            arrayOf(todayStart.toString(), appId?.toString(), appId?.toString())
        ).use { usedCursor ->
            if (usedCursor.moveToFirst()) usedCursor.getInt(0) else 0
        }

        val limit = db.rawQuery(
            """
        SELECT max_per_day_current FROM snooze_prefs
        WHERE scope = ? AND (? IS NULL OR app_id = ?)
        LIMIT 1
        """ .trimIndent(),
            arrayOf(scope, appId?.toString(), appId?.toString())
        ).use { prefCursor ->
            if (prefCursor.moveToFirst()) prefCursor.getInt(0) else 0
        }

        return (limit - used).coerceAtLeast(0)
    }

    /**
     * decrementSnoozeCount()
     * Logs a snooze usage for today.
     * Scope is either "global" or "per_app".
     * Optionally links to an event (like a popup) via event ID.
     *
     * @param scope "global" or "per_app"
     * @param appId ID of the app, if applicable
     * @param linkedEventId Optional ID from the events table
     */
    fun decrementSnoozeCount(scope: String, appId: Int? = null, linkedEventId: Int? = null) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("used_at_ms", System.currentTimeMillis())
            put("app_id", appId)
            put("reason", scope)
            put("linked_event_id", linkedEventId)
        }
        db.insert("snooze_ledger", null, values)
    }

    /**
     * logSnoozeUsed()
     * Logs a user's use of a snooze into the snooze_ledger table,
     * and creates a matching event entry in the events table for global tracking.
     *
     * This is typically triggered when a user presses "Snooze" on the interrupt popup.
     *
     * @param appId ID of the app the user snoozed (foreign key to apps table).
     * @param scope Whether the snooze is "per_app" or "global".
     */
    fun logSnoozeUsed(appId: Int, scope: String) {
        val db = writableDatabase
        // Insert into snooze_ledger for detailed per-snooze tracking
        val values = ContentValues().apply {
            put("used_at_ms", System.currentTimeMillis())
            put("app_id", appId)
            // put("scope", scope) -- REMOVED as per schema
            put("reason", scope) // Store scope in reason to match isSnoozeActive logic
        }
        db.insert("snooze_ledger", null, values)

        // Also log to the events table for broader reporting
        logEvent("snooze_used", appId = appId)
    }
}
