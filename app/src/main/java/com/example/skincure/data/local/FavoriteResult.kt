package com.example.skincure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "result")
data class FavoriteResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val diseaseName: String,
    val predictionScore: Float?,
    val description: String,
    val imageUri: String?
)
