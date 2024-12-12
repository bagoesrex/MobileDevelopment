package com.example.skincure.di

import android.content.Context
import com.example.skincure.data.local.AppDatabase
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.data.remote.retrofit.ApiConfig
import com.example.skincure.data.repository.Repository
import com.google.firebase.auth.FirebaseAuth

object Injection {
    fun provideRepository(context: Context): Repository {
        val pref = UserPreferences(context)
        val auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(context)
        val dao = database.resultDao()
        val apiService = ApiConfig.getApiService(pref)
        return Repository.getInstance(auth, apiService,dao, db = database)
    }
}