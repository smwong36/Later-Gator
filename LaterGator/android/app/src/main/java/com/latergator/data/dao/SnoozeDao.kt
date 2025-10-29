package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.SnoozePrefs
import com.latergator.data.entities.SnoozeLedger

@Dao
interface SnoozeDao {

    // ---- Snooze Prefs ----
    @Query("SELECT * FROM snooze_prefs LIMIT 1")
    suspend fun getSnoozePrefs(): SnoozePrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnoozePrefs(prefs: SnoozePrefs)

    // ---- Snooze Ledger ----
    @Query("SELECT * FROM snooze_ledger ORDER BY snoozeStartTime DESC")
    suspend fun getSnoozeHistory(): List<SnoozeLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnoozeEvent(event: SnoozeLedger)

    @Query("DELETE FROM snooze_ledger")
    suspend fun clearAllSnoozeEvents()
}
