package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reflection_prefs")
data class ReflectionPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val am_enabled: Boolean,
    val pm_enabled: Boolean,
    val am_time_local: String?,
    val pm_time_local: String?,
    val created_at_ms: Long
)

@Entity(tableName = "reflection_sessions")
data class ReflectionSessions(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val kind: String,
    val at_ms: Long,
    val for_date: String,
    val discipline_1_5: Int?,
    val motivation_1_5: Int?,
    val notes: String?
)
