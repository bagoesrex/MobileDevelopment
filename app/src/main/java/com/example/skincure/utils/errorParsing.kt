package com.example.skincure.utils

import com.google.gson.annotations.SerializedName
import retrofit2.HttpException

fun parseErrorMessage(exception: HttpException): String? {
    return try {
        val errorBody = exception.response()?.errorBody()?.string()
        val errorResponse = errorBody?.let {
            val gson = com.google.gson.Gson()
            gson.fromJson(it, ErrorResponse::class.java)
        }
        errorResponse?.message
    } catch (e: Exception) {
        null
    }
}

data class ErrorResponse(
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?
)