package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.*

@Dao
interface RewardDao {

    // --- Points Ledger ---
    @Query("SELECT * FROM points_ledger ORDER BY at_ms DESC")
    suspend fun getAllPointsLedger(): List<PointsLedger>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPointsLedger(entry: PointsLedger)


    // --- Streaks ---
    @Query("SELECT * FROM streaks ORDER BY updated_at_ms DESC")
    suspend fun getAllStreaks(): List<Streaks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: Streaks)


    // --- Badges ---
    @Query("SELECT * FROM badges ORDER BY id ASC")
    suspend fun getAllBadges(): List<Badges>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: Badges)


    // --- Earned Badges ---
    @Query("SELECT * FROM earned_badges ORDER BY earned_at_ms DESC")
    suspend fun getAllEarnedBadges(): List<EarnedBadges>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarnedBadge(earned: EarnedBadges)


    // --- Baseline Usage ---
    @Query("SELECT * FROM baseline_usage ORDER BY week_start_date DESC")
    suspend fun getAllBaselineUsage(): List<BaselineUsage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBaselineUsage(usage: BaselineUsage)
}
