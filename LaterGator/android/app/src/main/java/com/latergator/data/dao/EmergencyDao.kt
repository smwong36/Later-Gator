package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.*

@Dao
interface EmergencyDao {
    @Query("SELECT * FROM emergency_prefs LIMIT 1")
    suspend fun getEmergencyPrefs(): EmergencyPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyPrefs(prefs: EmergencyPrefs)

    @Query("SELECT * FROM emergency_allowed_apps")
    suspend fun getAllowedApps(): List<EmergencyAllowedApps>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllowedApp(app: EmergencyAllowedApps)
}
