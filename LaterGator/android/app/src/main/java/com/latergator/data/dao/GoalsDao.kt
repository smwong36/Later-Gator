package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.Goals

@Dao
interface GoalsDao {

    // --- Goals ---
    @Query("SELECT * FROM goals ORDER BY for_date DESC")
    suspend fun getAllGoals(): List<Goals>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goals)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)
}