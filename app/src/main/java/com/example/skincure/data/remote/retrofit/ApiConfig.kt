package com.example.skincure.data.remote.retrofit

import android.util.Log
import com.example.skincure.data.pref.UserPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.skincure.BuildConfig


class ApiConfig {
    companion object {

        private const val BASE_URL = BuildConfig.BASE_URL

        fun getApiService(userPreferences: UserPreferences): ApiService {
            val loggingInterceptor =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                } else {
                    HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.NONE)
                }

            val authInterceptor = Interceptor { chain ->
                val token = userPreferences.getToken() ?: ""

                Log.d("iniTokens", token)

                val req = chain.request()
                val requestHeaders = req.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
                chain.proceed(requestHeaders)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            return retrofit.create(ApiService::class.java)
        }
    }
}