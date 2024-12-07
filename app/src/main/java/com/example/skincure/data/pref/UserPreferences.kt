package com.example.skincure.data.pref

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {

    companion object {
        private const val TOKEN_KEY = "token"
        private const val USER_NAME_KEY = "user_name"
        private const val USER_AGE_KEY = "user_age"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    fun saveUser(name: String, age: Int) {
        sharedPreferences.edit().apply {
            putString(USER_NAME_KEY, name)
            putInt(USER_AGE_KEY, age)
            apply()
        }
    }

    fun saveNameSignUp(name: String) {
        sharedPreferences.edit().apply {
            putString(USER_NAME_KEY, name)
            apply()
        }
    }
    fun getUserName(): String? {
        return sharedPreferences.getString(USER_NAME_KEY, null)
    }
    fun getUserAge(): Int {
        return sharedPreferences.getInt(USER_AGE_KEY, 0)
    }

    // Menyimpan token
    fun saveToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    // Mengambil token
    fun getToken(): String? {
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    // Menghapus token
    fun clearToken() {
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }
}