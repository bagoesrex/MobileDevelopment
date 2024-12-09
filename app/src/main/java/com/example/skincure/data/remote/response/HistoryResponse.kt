package com.example.skincure.data.remote.response

data class HistoryResponse(
    val id: String,
    val diseaseName: String,
    val description: String,
    val imageUri: String,
    val timestamp: String,
)
