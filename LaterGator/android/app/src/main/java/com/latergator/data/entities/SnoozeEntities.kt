package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- Snooze Preferences ---
@Entity(tableName = "snooze_prefs")
data class SnoozePrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val duration_minutes: Int,
    val allow_multiple_snoozes: Int,
    val max_daily_snoozes: Int,
    val vibrate_on_end: Int,
    val sound_uri: String?,
    val created_at_ms: Long,
    val updated_at_ms: Long
)

// --- Snooze Ledger ---
@Entity(tableName = "snooze_ledger")
data class SnoozeLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val snooze_start_ms: Long,
    val snooze_end_ms: Long?,
    val duration_used_minutes: Int,
    val cancelled_flag: Int,
    val created_at_ms: Long,
    val updated_at_ms: Long
)