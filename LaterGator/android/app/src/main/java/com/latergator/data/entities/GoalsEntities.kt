package com.latergator.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// -----------------------------------------
// Goals
// -----------------------------------------
@Entity(tableName = "goals")
data class Goals(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val for_date: String,
    val text: String,
    val created_via: String,
    val status: String = "planned",
    val created_at_ms: Long,
    val completed_at_ms: Long?
)