package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.*

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_prefs LIMIT 1")
    suspend fun getCalendarPrefs(): CalendarPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarPrefs(prefs: CalendarPrefs)

    @Query("SELECT * FROM calendar_events_cache ORDER BY start_ms ASC")
    suspend fun getAllCalendarEvents(): List<CalendarEventsCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventsCache)
}
