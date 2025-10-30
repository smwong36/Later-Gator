package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_prefs")
data class CalendarPrefs(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val enabled: Boolean,
    val provider: String?,
    val sync_scope: String?,
    val last_sync_ms: Long?
)

@Entity(tableName = "calendar_events_cache")
data class CalendarEventsCache(
    @PrimaryKey val provider_event_id: String,
    val title: String?,
    val start_ms: Long?,
    val end_ms: Long?,
    val busy: Boolean,
    val source_calendar: String?
)
