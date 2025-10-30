package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_prefs")
data class SleepPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enabled: Boolean,
    val bedtime_local: String?,
    val window_minutes: Int,
    val nudge_after_bedtime: Boolean,
    val created_at_ms: Long
)

@Entity(tableName = "sleep_nudges_ledger")
data class SleepNudgesLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val at_ms: Long,
    val kind: String,                           // 'winddown'|'bedtime'|'after_bedtime'
    val app_id: Int?,
    val action_taken: String?                   // 'dismissed'|'opened_app'|'opened_settings'
)
