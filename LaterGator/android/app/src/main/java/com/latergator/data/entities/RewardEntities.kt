package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points_ledger")
data class PointsLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val at_ms: Long,
    val delta: Int,
    val reason: String,                             // 'goal_met'|'streak'|'focus_interval'|'weekly_improvement'
    val meta_json: String?
)

@Entity(tableName = "streaks")
data class Streaks(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,                               // 'daily_focus'|'goal_completion'
    val current_len: Int,
    val longest_len: Int,
    val updated_at_ms: Long
)

@Entity(tableName = "badges")
data class Badges(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val slug: String,
    val name: String,
    val description: String,
    val asset_key: String,                          // maps to bundled asset
    val criteria_json: String?
)

@Entity(tableName = "earned_badges")
data class EarnedBadges(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val badge_id: Int?,
    val earned_at_ms: Long,
    val evidence_json: String?
)

@Entity(tableName = "baseline_usage")
data class BaselineUsage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val metric: String,                             // 'weekly_minutes_all'|'weekly_minutes_by_app'
    val week_start_date: String,
    val app_id: Int?,
    val value_minutes: Int
)
