package com.latergator.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "latergator.db"
        private const val DB_VERSION = 3

        // Table Names
        private const val TABLE_PROFILE = "Profile"
        private const val TABLE_TRACKED_APPS = "TrackedApps"

        // Profile Table Columns
        private const val COL_ID = "id"
        private const val COL_NAME = "name"

        // Tracked Apps Table Columns
        private const val COL_PACKAGE_NAME = "package_name"
        private const val COL_TIME_LIMIT = "time_limit_minutes"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createProfileTable = "CREATE TABLE $TABLE_PROFILE (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COL_NAME TEXT)"
        db?.execSQL(createProfileTable)

        val createTrackedAppsTable = "CREATE TABLE $TABLE_TRACKED_APPS (" +
                "$COL_PACKAGE_NAME TEXT PRIMARY KEY," +
                "$COL_TIME_LIMIT INTEGER NOT NULL DEFAULT 30)"
        db?.execSQL(createTrackedAppsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRACKED_APPS")
        onCreate(db)
    }

    // --- Profile Functions ---

    fun saveUserProfile(name: String) {
        val db = this.writableDatabase
        db.delete(TABLE_PROFILE, null, null) // Clear old profile data
        val values = ContentValues().apply {
            put(COL_NAME, name)
        }
        db.insert(TABLE_PROFILE, null, values)
    }

    fun getUserProfileName(): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COL_NAME FROM $TABLE_PROFILE LIMIT 1", null)
        var name: String? = null
        try {
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(COL_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
        } finally {
            cursor.close()
        }
        return name
    }

    // --- Tracked App Functions ---

    fun addTrackedApp(packageName: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_PACKAGE_NAME, packageName)
        }
        db.insertWithOnConflict(TABLE_TRACKED_APPS, null, values, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun removeTrackedApp(packageName: String) {
        val db = this.writableDatabase
        db.delete(TABLE_TRACKED_APPS, "$COL_PACKAGE_NAME = ?", arrayOf(packageName))
    }

    fun updateTimeLimit(packageName: String, timeLimitMinutes: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_TIME_LIMIT, timeLimitMinutes)
        }
        db.update(TABLE_TRACKED_APPS, values, "$COL_PACKAGE_NAME = ?", arrayOf(packageName))
    }

    fun getTrackedApps(): Map<String, Int> {
        val trackedApps = mutableMapOf<String, Int>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRACKED_APPS", null)
        try {
            if (cursor.moveToFirst()) {
                val packageIndex = cursor.getColumnIndex(COL_PACKAGE_NAME)
                val timeLimitIndex = cursor.getColumnIndex(COL_TIME_LIMIT)
                if (packageIndex != -1 && timeLimitIndex != -1) {
                    do {
                        val packageName = cursor.getString(packageIndex)
                        val timeLimit = cursor.getInt(timeLimitIndex)
                        trackedApps[packageName] = timeLimit
                    } while (cursor.moveToNext())
                }
            }
        } finally {
            cursor.close()
        }
        return trackedApps
    }
}
