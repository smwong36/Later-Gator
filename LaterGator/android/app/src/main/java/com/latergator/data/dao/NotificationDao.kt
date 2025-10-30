package com.latergator.data.dao

import androidx.room.*
import com.latergator.data.entities.NotificationPrefs

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notification_prefs LIMIT 1")
    suspend fun getNotificationPrefs(): NotificationPrefs?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationPrefs(prefs: NotificationPrefs)
}
