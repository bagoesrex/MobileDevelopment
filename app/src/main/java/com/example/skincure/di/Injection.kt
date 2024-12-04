package com.example.skincure.di

import android.content.Context
import com.example.skincure.data.local.AppDatabase
import com.example.skincure.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth

object Injection {
    fun provideRepository(context: Context): AuthRepository {
        val auth = FirebaseAuth.getInstance()
        val database = AppDatabase.getDatabase(context)
        val dao = database.resultDao()
        return AuthRepository.getInstance(auth, dao)
    }
}