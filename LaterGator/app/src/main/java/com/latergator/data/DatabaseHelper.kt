package com.latergator.data

import android.content.Context
import android.content.ContentValues
import android.content.pm.PackageManager
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

    private var db: SQLiteDatabase? = null

    private fun dbPath(): String {
        return context.applicationInfo.dataDir + DB_PATH_SUFFIX + DB_NAME
    }

    init {
        createDatabaseIfNotExists()
    }

    private fun createDatabaseIfNotExists() {
        val dbFile = context.getDatabasePath(DB_NAME)
        if (!dbFile.exists()) {
            this.readableDatabase // Creates empty DB
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

    fun open(): SQLiteDatabase {
        db = SQLiteDatabase.openDatabase(dbPath(), null, SQLiteDatabase.OPEN_READWRITE)
        return db as SQLiteDatabase
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
            db.close()
        }
    }

    fun getTrackedApps(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT package_name FROM apps WHERE is_tracked = 1", null)
        return if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            null
        }.also {
            cursor.close()
            db.close()
        }
    }

    fun getAllTrackedAppLimits(): Map<String, Int> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT package_name, time_limit_min FROM apps WHERE is_tracked = 1", null)
        val trackedApps = mutableMapOf<String, Int>()

        if (cursor.moveToFirst()) {
            do {
                val packageName = cursor.getString(cursor.getColumnIndexOrThrow("package_name"))
                val timeLimit = cursor.getInt(cursor.getColumnIndexOrThrow("time_limit_min"))
                trackedApps[packageName] = timeLimit
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return trackedApps
    }

    fun updateTimeLimit(packageName: String, newLimit: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("time_limit_min", newLimit)
        }
        db.update(
            "apps",                   // Table name
            values,                   // New values
            "package_name = ?",       // WHERE clause
            arrayOf(packageName)      // WHERE args
        )
        db.close()
    }

    @Synchronized
    override fun close() {
        db?.close()
        super.close()
    }
}