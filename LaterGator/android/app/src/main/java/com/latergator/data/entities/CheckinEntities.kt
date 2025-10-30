package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// -----------------------------------------
// Check-in Preferences
// -----------------------------------------
@Entity(tableName = "checkin_prefs")
data class CheckinPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enabled: Boolean,
    val daily_max: Int = 0,
    val window_start_local: String?,
    val window_end_local: String?,
    val created_at_ms: Long
)

// -----------------------------------------
// Check-in Ledger
// -----------------------------------------
@Entity(tableName = "checkin_ledger")
data class CheckinLedger(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompted_at_ms: Long,
    val goal_id: Int?,              // FK to goals.id (hybrid)
    val question_text: String,
    val response: String?,
    val responded_at_ms: Long?,
    val linked_snooze_id: Int?      // FK to snooze_ledger.id (hybrid)
)
