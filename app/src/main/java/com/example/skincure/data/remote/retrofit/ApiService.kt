package com.example.skincure.data.remote.retrofit

import com.example.skincure.data.remote.response.NewsResponse
import com.example.skincure.data.remote.response.PredictHistoriesResponse
import com.example.skincure.data.remote.response.PredictUploadResponse
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("api/predict")
    suspend fun predictUpload(
        @Part file: MultipartBody.Part,
    ): PredictUploadResponse

    @GET("api/predict/histories")
    suspend fun getPredictHistories(
        @Query("uid") uid: String
    ): PredictHistoriesResponse

    @GET("api/news")
    suspend fun getAllNews(): NewsResponse
}

