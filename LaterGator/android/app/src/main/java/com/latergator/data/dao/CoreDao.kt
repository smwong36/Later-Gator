package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.*

@Dao
interface CoreDao {

    // --- Profile ---
    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getProfile(): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile)

    // --- Apps ---
    @Query("SELECT * FROM apps ORDER BY label")
    suspend fun getAllApps(): List<App>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: App)

    // --- Time Limit Prefs ---
    @Query("SELECT * FROM time_limit_prefs WHERE scope = :scope")
    suspend fun getTimeLimitPrefs(scope: String): List<TimeLimitPref>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeLimitPref(pref: TimeLimitPref)

    // --- Time Limit Hits ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun logLimitHit(hit: TimeLimitHit)

    @Query("SELECT * FROM time_limit_hits_ledger ORDER BY at_ms DESC LIMIT 50")
    suspend fun getRecentHits(): List<TimeLimitHit>

    // --- Usage Sessions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: UsageSession)

    @Query("SELECT * FROM usage_sessions WHERE app_id = :appId ORDER BY started_at_ms DESC LIMIT 100")
    suspend fun getSessionsForApp(appId: Int): List<UsageSession>

    // --- Events ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Query("SELECT * FROM events ORDER BY at_ms DESC LIMIT 100")
    suspend fun getRecentEvents(): List<Event>

    // --- Weekly Snapshots ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: WeeklySnapshot)

    @Query("SELECT * FROM weekly_snapshots ORDER BY week_start_date DESC LIMIT 20")
    suspend fun getRecentSnapshots(): List<WeeklySnapshot>
}
