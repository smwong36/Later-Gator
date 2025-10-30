package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_prefs")
data class NotificationPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val max_per_day: Int,
    val max_per_week: Int,
    val quiet_start_local: String?,
    val quiet_end_local: String?,
    val reminders_enabled: Boolean,
    val channel_caps_json: String?,
    val created_at_ms: Long,
    val updated_at_ms: Long
)
