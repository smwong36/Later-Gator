package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_prefs")
data class EmergencyPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enabled: Boolean,
    val activation_note: String?,
    val activated_at_ms: Long?,
    val deactivated_at_ms: Long?,
    val priority: Int,
    val created_at_ms: Long,
    val updated_at_ms: Long
)

@Entity(tableName = "emergency_allowed_apps")
data class EmergencyAllowedApps(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val app_id: Int,
    val notes: String?
)
