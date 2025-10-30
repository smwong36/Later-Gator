package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.SnoozePrefs
import com.latergator.data.entities.SnoozeLedger

@Dao
interface SnoozeDao {

    // --- Preferences ---
    @Query("SELECT * FROM snooze_prefs LIMIT 1")
    suspend fun getSnoozePrefs(): SnoozePrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnoozePrefs(prefs: SnoozePrefs)

    @Update
    suspend fun updateSnoozePrefs(prefs: SnoozePrefs)

    @Query("DELETE FROM snooze_prefs")
    suspend fun clearSnoozePrefs()


    // --- Ledger ---
    @Query("SELECT * FROM snooze_ledger ORDER BY snooze_start_ms DESC")
    suspend fun getAllSnoozeEvents(): List<SnoozeLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnoozeEvent(event: SnoozeLedger)

    @Query("DELETE FROM snooze_ledger WHERE id = :id")
    suspend fun deleteSnoozeEvent(id: Int)

    @Query("DELETE FROM snooze_ledger")
    suspend fun clearSnoozeLedger()
}