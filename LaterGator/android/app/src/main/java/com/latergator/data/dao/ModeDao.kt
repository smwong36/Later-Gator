package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.ModeSettings

@Dao
interface ModeDao {

    @Query("SELECT * FROM mode_settings ORDER BY createdAt DESC")
    suspend fun getAllModes(): List<ModeSettings>

    @Query("SELECT * FROM mode_settings WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveMode(): ModeSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMode(mode: ModeSettings)

    @Update
    suspend fun updateMode(mode: ModeSettings)

    @Query("DELETE FROM mode_settings WHERE id = :id")
    suspend fun deleteModeById(id: Int)
}
