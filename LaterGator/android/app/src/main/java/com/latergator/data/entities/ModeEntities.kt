package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mode_settings")
data class ModeSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mode_name: String,
    val description: String?,
    val active_flag: Int,
    val focus_level: Int,
    val allow_notifications: Int,
    val allow_calls: Int,
    val allow_sms: Int,
    val block_social_apps: Int,
    val block_gaming_apps: Int,
    val block_video_apps: Int,
    val block_productivity_apps: Int,
    val auto_start_time: String?,
    val auto_end_time: String?,
    val repeat_days: String?,
    val created_at_ms: Long,
    val updated_at_ms: Long
)