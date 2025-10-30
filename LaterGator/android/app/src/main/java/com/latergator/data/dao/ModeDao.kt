package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.ModeSettings

@Dao
interface ModeDao {

    // --- Retrieve all modes ---
    @Query("SELECT * FROM mode_settings ORDER BY created_at_ms DESC")
    suspend fun getAllModes(): List<ModeSettings>

    // --- Retrieve the currently active mode (if any) ---
    @Query("SELECT * FROM mode_settings WHERE active_flag = 1 LIMIT 1")
    suspend fun getActiveMode(): ModeSettings?

    // --- Insert or replace a mode ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeSettings)

    // --- Update a mode ---
    @Update
    suspend fun updateMode(mode: ModeSettings)

    // --- Delete a mode by ID ---
    @Query("DELETE FROM mode_settings WHERE id = :id")
    suspend fun deleteModeById(id: Int)

    // --- Clear all modes ---
    @Query("DELETE FROM mode_settings")
    suspend fun clearAllModes()


    // ==========================================================
    // 🔄  Mode activation logic
    // ==========================================================

    // --- Deactivate all modes ---
    @Query("UPDATE mode_settings SET active_flag = 0")
    suspend fun deactivateAllModes()

    // --- Activate a specific mode by ID ---
    @Query("UPDATE mode_settings SET active_flag = 1 WHERE id = :id")
    suspend fun activateModeById(id: Int)

    // --- Toggle a mode’s active state (switch on/off) ---
    @Query("""
        UPDATE mode_settings 
        SET active_flag = CASE WHEN active_flag = 1 THEN 0 ELSE 1 END 
        WHERE id = :id
    """)
    suspend fun toggleModeById(id: Int)
}
