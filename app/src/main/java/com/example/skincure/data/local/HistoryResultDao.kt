package com.example.skincure.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.skincure.data.remote.response.HistoriesItem

@Dao
interface HistoryResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: List<HistoriesItem>)

    @Query("SELECT * FROM histories WHERE uid = :id")
    suspend fun getStoryById(id: String): HistoriesItem

    @Query("SELECT * FROM histories")
    fun getAllStory(): PagingSource<Int, HistoriesItem>

    @Query("DELETE FROM histories")
    suspend fun deleteStory()
}