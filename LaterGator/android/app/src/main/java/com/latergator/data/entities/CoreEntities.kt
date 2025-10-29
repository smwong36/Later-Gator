package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// -------------------------------------------
// Admin / Versioning
// -------------------------------------------
@Entity(tableName = "schema_migrations")
data class SchemaMigration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val version: Int,
    val applied_at_ms: Long
)

// -------------------------------------------
// Core Tables
// -------------------------------------------
@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tz: String,
    val weekly_goal_minutes: Int,
    val privacy_level: String,
    val created_at_ms: Long,
    val updated_at_ms: Long
)

@Entity(tableName = "apps")
data class App(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val package_name: String,
    val label: String,
    val category: String?,
    val is_tracked: Int,
    val created_at_ms: Long,
    val updated_at_ms: Long
)

@Entity(tableName = "time_limit_prefs")
data class TimeLimitPref(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scope: String,
    val app_id: Int?,
    val daily_limit_minutes_current: Int?,
    val weekly_limit_minutes_current: Int?,
    val daily_limit_minutes_original: Int?,
    val weekly_limit_minutes_original: Int?,
    val active: Int,
    val created_at_ms: Long,
    val updated_at_ms: Long,
    val notes: String?
)

@Entity(tableName = "time_limit_hits_ledger")
data class TimeLimitHit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val at_ms: Long,
    val scope: String,
    val app_id: Int?,
    val period: String,
    val limit_minutes: Int,
    val used_minutes: Int,
    val action_taken: String,
    val meta_json: String?
)

@Entity(tableName = "usage_sessions")
data class UsageSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val app_id: Int,
    val started_at_ms: Long,
    val ended_at_ms: Long,
    val duration_ms: Long,
    val source: String,
    val blocked_flag: Int,
    val notes: String?
)

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val at_ms: Long,
    val kind: String,
    val app_id: Int?,
    val meta_json: String?
)

@Entity(tableName = "weekly_snapshots")
data class WeeklySnapshot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val week_start_date: String,
    val app_id: Int?,
    val total_minutes: Int,
    val sessions_count: Int,
    val interventions_count: Int,
    val avg_session_minutes: Double,
    val created_at_ms: Long
)
