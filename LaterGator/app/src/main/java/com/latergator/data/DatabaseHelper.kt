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

    private fun dbPath(): String {
        return context.applicationInfo.dataDir + DB_PATH_SUFFIX + DB_NAME
    }

    private fun createDatabaseIfNotExists() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            this.readableDatabase.close() // Creates empty DB, then close it to copy
            copyDatabaseFromAssets()
        }
    }

    private fun copyDatabaseFromAssets() {
        try {
            val inputStream: InputStream = context.assets.open(DB_NAME)
            val outFileName = dbPath()
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

    fun hasProfile(): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id FROM profile LIMIT 1", null)
        return cursor.moveToFirst().also {
            cursor.close()
        }
    }

    fun createUserProfile(userName: String, tz: String) {
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

    fun updateUserProfileName(newName: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_name", newName)
            put("updated_at_ms", System.currentTimeMillis())
        }
        db.update("profile", values, null, null)
    }

    fun getUserProfileName(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT user_name FROM profile LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    fun getTrackedApps(): Map<String, Int?> {
        val db = readableDatabase
        val trackedApps = mutableMapOf<String, Int?>()
        val query = """
            SELECT a.package_name, t.daily_limit_minutes_current
            FROM apps a
            LEFT JOIN time_limit_prefs t ON a.id = t.app_id AND t.scope = 'per_app'
            WHERE a.is_tracked = 1
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        try {
            if (cursor.moveToFirst()) {
                do {
                    val packageNameIndex = cursor.getColumnIndex("package_name")
                    val timeLimitIndex = cursor.getColumnIndex("daily_limit_minutes_current")
                    if (packageNameIndex != -1) {
                        val packageName = cursor.getString(packageNameIndex)
                        val timeLimit = if (timeLimitIndex != -1 && !cursor.isNull(timeLimitIndex)) {
                            cursor.getInt(timeLimitIndex)
                        } else {
                            null // Correctly use null for "No Limit"
                        }
                        trackedApps[packageName] = timeLimit
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor.close()
        }
        return trackedApps
    }

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
                } else {
                    put("daily_limit_minutes_current", newLimit)
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
                values.put("daily_limit_minutes_original", newLimit)
                db.insertWithOnConflict("time_limit_prefs", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    /** ==================================
     * EVENT LOG HANDLERS
     * ==================================
     */
    /**
     * logEvent()
     * Logs a user action or system event into the events table.
     *
     * @param eventType Type of the event (e.g., "snooze_used", "manual_close").
     * @param appId Optional ID of the app associated with the event, if applicable.
     */
    fun logEvent(eventType: String, appId: Int? = null) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("event_type", eventType)
            put("timestamp_ms", System.currentTimeMillis())
            if (appId != null) put("app_id", appId)
        }
        db.insert("events", null, values)
        db.close()
    }

    /** ==================================
     * INTERRUPTION / POPUP HANDLERS
     * ==================================
     */

    /**
     * incrementUsage()
     * Logs a new app usage session and creates an event entry for it.
     *
     * @param appId The ID of the app being used.
     * @param startTimeMs The start time of the session in milliseconds.
     * @param endTimeMs The end time of the session in milliseconds.
     * @return True if both inserts were successful, false otherwise.
     */
    fun incrementUsage(appId: Int, startTimeMs: Long, endTimeMs: Long): Boolean {
        val durationMs = endTimeMs - startTimeMs
        val db = writableDatabase

        return try {
            db.beginTransaction()

            // Insert into usage_sessions
            val usageValues = ContentValues().apply {
                put("app_id", appId)
                put("start_time_ms", startTimeMs)
                put("end_time_ms", endTimeMs)
                put("duration_ms", durationMs)
            }
            val usageInsertResult = db.insert("usage_sessions", null, usageValues)

            // Insert into events
            val eventValues = ContentValues().apply {
                put("event_type", "app_usage")
                put("event_time_ms", endTimeMs)
                put("associated_id", appId)
            }
            val eventInsertResult = db.insert("events", null, eventValues)

            if (usageInsertResult != -1L && eventInsertResult != -1L) {
                db.setTransactionSuccessful()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.endTransaction()
            db.close()
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

        // Reset snoozes for the day
        db.execSQL("""
        UPDATE snooze_settings 
        SET daily_snoozes_remaining = daily_snoozes_original
    """)

        db.close()
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
        WHERE app_id = ? AND started_at_ms >= ? AND ended_at_ms IS NOT NULL
    """
        val cursor = db.rawQuery(query, arrayOf(appId.toString(), startOfDay.toString()))

        var totalTimeMinutes = 0
        if (cursor.moveToFirst()) {
            val durationMs = cursor.getLong(cursor.getColumnIndexOrThrow("total_usage"))
            totalTimeMinutes = (durationMs / 60000).toInt()  // Convert ms to minutes
        }

        cursor.close()
        db.close()
        return totalTimeMinutes
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
        val cursor = db.rawQuery(
            """
        SELECT start_time_local 
        FROM mode_settings 
        WHERE enabled = 1 AND start_time_local IS NOT NULL 
        ORDER BY priority DESC LIMIT 1
        """.trimIndent(), null
        )

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

        cursor.close()
        db.close()
        return hour
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

        val cursor = db.rawQuery(query, arrayOf(appId.toString()))
        var limit: Int? = null

        if (cursor.moveToFirst()) {
            if (!cursor.isNull(0)) {
                limit = cursor.getInt(0)
            }
        }

        cursor.close()
        db.close()
        return limit
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

        db.rawQuery(query, arrayOf(startOfDay.toString())).use { cursor ->
            if (cursor.moveToFirst()) {
                val totalMs = cursor.getLong(0)
                return (totalMs / 60000).toInt()  // Convert ms → minutes
            }
        }

        return 0
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

        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToFirst()) {
                return if (!cursor.isNull(0)) cursor.getInt(0) else null
            }
        }

        return null
    }

    /**
     * logTimeLimitBreach()
     * Logs a time limit breach into the time_limit_hits_ledger table.
     * Called when an app exceeds its daily time limit.
     *
     * @param appId The ID of the app from the apps table.
     * @param durationMs The total usage duration in milliseconds that caused the breach.
     */
    fun logTimeLimitBreach(appId: Int, durationMs: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("app_id", appId)
            put("breach_timestamp_ms", System.currentTimeMillis())
            put("duration_ms", durationMs)
        }
        db.insert("time_limit_hits_ledger", null, values)
        db.close()
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

        // Figure out how many have been used today
        val usedCursor = db.rawQuery(
            """
        SELECT COUNT(*) FROM snooze_ledger
        WHERE used_at_ms >= ? AND (? IS NULL OR app_id = ?)
        """.trimIndent(),
            arrayOf(todayStart.toString(), appId?.toString(), appId?.toString())
        )
        val used = if (usedCursor.moveToFirst()) usedCursor.getInt(0) else 0
        usedCursor.close()

        // Get daily limit from prefs
        val prefCursor = db.rawQuery(
            """
        SELECT max_per_day_current FROM snooze_prefs
        WHERE scope = ? AND (? IS NULL OR app_id = ?)
        LIMIT 1
        """.trimIndent(),
            arrayOf(scope, appId?.toString(), appId?.toString())
        )
        val limit = if (prefCursor.moveToFirst()) prefCursor.getInt(0) else 0
        prefCursor.close()

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
}
