package com.latergator.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.latergator.data.dao.*
import com.latergator.data.entities.*

@Database(
    entities = [
        // Core pack
        SchemaMigration::class,
        Profile::class,
        App::class,
        TimeLimitPref::class,
        TimeLimitHit::class,
        UsageSession::class,
        Event::class,
        WeeklySnapshot::class,
        ModeSettings::class,
        SnoozePrefs::class,
        SnoozeLedger::class,
        PomodoroPrefs::class,
        PomodoroLedger::class,
        ReflectionPrefs::class,
        ReflectionSessions::class,
        Goals::class,
        CheckinPrefs::class,
        CheckinLedger::class
        // Additional packs will be added here as you import them (Mode, Snooze, etc.)
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    // DAO references
    abstract fun coreDao(): CoreDao
    abstract fun modeDao(): ModeDao
    abstract fun snoozeDao(): SnoozeDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun reflectionsDao(): ReflectionsDao
    abstract fun goalsDao(): GoalsDao
    abstract fun checkinDao(): CheckinDao
    // (each new pack will add its DAO method here later)

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "latergator.db"
                )
                    // ✅ Creates your database using 0001_init.sql on first install
                    .createFromAsset("databases/0001_init.sql")
                    // ✅ For prototypes, auto-resets schema on mismatch instead of crashing
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
