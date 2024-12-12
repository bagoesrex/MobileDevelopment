package com.example.skincure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val result: HistoryResult,
    val predictionScore: Double,
    val description: String,
    val imageUri: String?,
    val timestamp: String,
)