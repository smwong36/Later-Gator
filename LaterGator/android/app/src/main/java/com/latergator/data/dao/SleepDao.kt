package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.*

@Dao
interface SleepDao {

    // --- Sleep Prefs ---
    @Query("SELECT * FROM sleep_prefs LIMIT 1")
    suspend fun getSleepPrefs(): SleepPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepPrefs(prefs: SleepPrefs)

    // --- Sleep Nudges Ledger ---
    @Query("SELECT * FROM sleep_nudges_ledger ORDER BY at_ms DESC")
    suspend fun getAllSleepNudges(): List<SleepNudgesLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepNudge(nudge: SleepNudgesLedger)

    @Query("DELETE FROM sleep_nudges_ledger")
    suspend fun clearSleepNudgesLedger()
}
