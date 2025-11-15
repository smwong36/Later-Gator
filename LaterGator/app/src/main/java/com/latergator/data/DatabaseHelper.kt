package com.latergator.data

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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
        val updatedRows = db.update("profile", values, null, null)
        return updatedRows > 0
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
            LEFT JOIN time_limit_prefs t ON a.id = t.app_id AND t.scope = 'per_app' AND t.active = 1
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

    fun setAppTrackingAndActiveStatus(packageName: String, isTracked: Boolean) {
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
}
