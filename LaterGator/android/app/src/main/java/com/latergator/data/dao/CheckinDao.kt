package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.CheckinPrefs
import com.latergator.data.entities.CheckinLedger

@Dao
interface CheckinDao {

    // --- Check-in Preferences ---
    @Query("SELECT * FROM checkin_prefs LIMIT 1")
    suspend fun getCheckinPrefs(): CheckinPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckinPrefs(prefs: CheckinPrefs)

    // --- Check-in Ledger ---
    @Query("SELECT * FROM checkin_ledger ORDER BY prompted_at_ms DESC")
    suspend fun getAllCheckins(): List<CheckinLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckin(entry: CheckinLedger)

    @Query("DELETE FROM checkin_ledger WHERE id = :id")
    suspend fun deleteCheckin(id: Int)
}
