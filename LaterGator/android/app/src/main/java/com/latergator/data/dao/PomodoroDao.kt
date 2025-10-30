package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.PomodoroPrefs
import com.latergator.data.entities.PomodoroLedger

@Dao
interface PomodoroDao {

    // --- Preferences ---
    @Query("SELECT * FROM pomodoro_prefs LIMIT 1")
    suspend fun getPomodoroPrefs(): PomodoroPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroPrefs(prefs: PomodoroPrefs)

    @Update
    suspend fun updatePomodoroPrefs(prefs: PomodoroPrefs)

    @Query("DELETE FROM pomodoro_prefs")
    suspend fun clearPomodoroPrefs()


    // --- Ledger ---
    @Query("SELECT * FROM pomodoro_ledger ORDER BY session_start_ms DESC")
    suspend fun getAllPomodoroSessions(): List<PomodoroLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroLedger)

    @Query("DELETE FROM pomodoro_ledger WHERE id = :id")
    suspend fun deletePomodoroSession(id: Int)

    @Query("DELETE FROM pomodoro_ledger")
    suspend fun clearPomodoroLedger()
}
