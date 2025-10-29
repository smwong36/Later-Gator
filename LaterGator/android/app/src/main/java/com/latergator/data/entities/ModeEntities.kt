package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mode_settings")
data class ModeSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val modeName: String,
    val description: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long?
)