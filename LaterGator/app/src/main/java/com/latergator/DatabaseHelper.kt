package com.latergator

import android.content.Context
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
        private const val DB_PATH = "databases"
    }

    private var db: SQLiteDatabase? = null

    init {
        copyDatabaseIfNeeded()
    }

    private fun copyDatabaseIfNeeded() {
        val dbPath = context.getDatabasePath(DB_NAME)

        if (!dbPath.exists()) {
            dbPath.parentFile?.mkdirs()

            try {
                val inputStream: InputStream = context.assets.open(DB_NAME)
                val outputStream: OutputStream = FileOutputStream(dbPath)

                val buffer = ByteArray(1024)
                var length: Int
                while (inputStream.read(buffer).also { length = it } > 0) {
                    outputStream.write(buffer, 0, length)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

            } catch (e: IOException) {
                throw RuntimeException("Error copying database", e)
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // not used
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // not used
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        if (db == null || !db!!.isOpen) {
            db = SQLiteDatabase.openDatabase(
                context.getDatabasePath(DB_NAME).path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }
        return db!!
    }
}
