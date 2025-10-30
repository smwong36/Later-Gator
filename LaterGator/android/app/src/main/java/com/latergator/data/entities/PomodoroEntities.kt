package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_prefs")
data class PomodoroPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val work_minutes: Int,
    val short_break_minutes: Int,
    val long_break_minutes: Int,
    val intervals_per_long_break: Int,
    val auto_start_next: Boolean,
    val daily_goal_intervals: Int,
    val sound_enabled: Boolean,
    val sound_key: String?,
    val sound_volume: Int,
    val mascot_theme_key: String?,
    val created_at_ms: Long
)

@Entity(tableName = "pomodoro_ledger")
data class PomodoroLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val started_at_ms: Long,
    val ended_at_ms: Long,
    val kind: String,
    val planned_minutes: Int,
    val actual_minutes: Int,
    val outcome: String,
    val interruption_reason: String?,
    val app_id: Int?,
    val notes: String?,
    val created_at_ms: Long
)
