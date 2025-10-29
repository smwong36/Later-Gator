package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snooze_prefs")
data class SnoozePrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val defaultDurationMinutes: Int,
    val maxDailySnoozes: Int,
    val resetTimeMillis: Long
)

@Entity(tableName = "snooze_ledger")
data class SnoozeLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val snoozeStartTime: Long,
    val snoozeEndTime: Long?,
    val reason: String?,
    val resumedAt: Long?,
    val modeId: Int? // links to ModeSettings if applicable
)
