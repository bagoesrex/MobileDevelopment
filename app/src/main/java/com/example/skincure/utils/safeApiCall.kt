package com.example.skincure.utils

import com.example.skincure.data.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            Result.Success(response)
        } catch (e: IOException) {
            Result.Error("Network error: ${e.message}")
        } catch (e: HttpException) {
            val errorMessage = parseErrorMessage(e)
            Result.Error(errorMessage ?: "Error: ${e.message}")
        } catch (e: Exception) {
            Result.Error("An unexpected error occurred: ${e.message}")
        }
    }
}