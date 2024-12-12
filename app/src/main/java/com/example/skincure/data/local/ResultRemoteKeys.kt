package com.example.skincure.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class ResultRemoteKeys(
    @PrimaryKey val id: String,
    val prevKey: Int?,
    val nextKey: Int?,
)