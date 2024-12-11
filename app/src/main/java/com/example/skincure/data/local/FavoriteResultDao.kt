package com.example.skincure.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface FavoriteResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: FavoriteResult)

    @Update
    suspend fun update(result: FavoriteResult)

    @Delete
    suspend fun delete(result: FavoriteResult)

    @Query("SELECT * FROM result WHERE imageUri = :imageUri LIMIT 1")
    suspend fun deleteByImageUri(imageUri: String): FavoriteResult?


    @Query("SELECT * FROM result WHERE id = :id LIMIT 1")
    suspend fun getEventById(id: Long): FavoriteResult?

    @Query("SELECT * FROM result WHERE imageUri = :imageUri LIMIT 1")
    fun getResultByImageUri(imageUri: String): LiveData<FavoriteResult>

    @Query("SELECT * FROM result")
    suspend fun getAllEvents(): List<FavoriteResult>
}
