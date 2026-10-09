package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entries")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolType: String, // "CHECKER", "HUMANIZE", "AI_STYLE", "IMPROVE"
    val title: String,
    val inputText: String,
    val outputText: String,
    val styleSelected: String,
    val aiScore: Int = 0,
    val humanScore: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val wordCount: Int = 0
)
