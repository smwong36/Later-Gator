package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_prefs")
data class SecurityPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val app_lock_enabled: Boolean,
    val lock_method: String?,
    val last_unlock_at_ms: Long?,
    val failed_attempts: Int
)
