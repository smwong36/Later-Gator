package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.SecurityPrefs

@Dao
interface SecurityDao {
    @Query("SELECT * FROM security_prefs LIMIT 1")
    suspend fun getSecurityPrefs(): SecurityPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityPrefs(prefs: SecurityPrefs)
}
