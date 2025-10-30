package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.*

@Dao
interface ReflectionsDao {

    // --- Reflection Prefs ---
    @Query("SELECT * FROM reflection_prefs LIMIT 1")
    suspend fun getReflectionPrefs(): ReflectionPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflectionPrefs(prefs: ReflectionPrefs)

    // --- Reflection Sessions ---
    @Query("SELECT * FROM reflection_sessions ORDER BY for_date DESC")
    suspend fun getAllReflectionSessions(): List<ReflectionSessions>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflectionSession(session: ReflectionSessions)

    @Query("DELETE FROM reflection_sessions")
    suspend fun clearReflectionSessions()
}
