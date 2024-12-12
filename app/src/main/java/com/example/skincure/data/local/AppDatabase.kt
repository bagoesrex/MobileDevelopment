package com.example.skincure.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.skincure.data.remote.response.HistoriesItem

@Database(entities = [FavoriteResult::class, ResultRemoteKeys::class, HistoriesItem::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun resultDao(): FavoriteResultDao
    abstract fun remoteKeysDao(): ResultRemoteKeysDao
    abstract fun historyDao(): HistoryResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
