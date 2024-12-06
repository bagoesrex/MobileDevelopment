package com.example.skincure.data.remote.response

data class NewsResponse(
    val title: String,
    val description: String,
    val imageUrl: String,
    val createdAt: String?,
)