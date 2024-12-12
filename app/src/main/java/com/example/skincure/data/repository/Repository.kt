package com.example.skincure.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.local.FavoriteResultDao
import com.example.skincure.data.remote.response.ContactUsRequest
import com.example.skincure.data.remote.response.ContactUsResponse
import com.example.skincure.data.remote.response.NewsResponse
import com.example.skincure.data.remote.response.PredictHistoriesResponse
import com.example.skincure.data.remote.response.PredictUploadResponse
import com.example.skincure.data.remote.retrofit.ApiService
import com.example.skincure.utils.safeApiCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import kotlin.Result
import com.example.skincure.data.Result as utilResult

class Repository(
    private val auth: FirebaseAuth,
    private val apiService: ApiService,
    private val dao: FavoriteResultDao
) {

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }


    suspend fun loginWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmailPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        }
    }

    suspend fun authWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            Result.success(result.user!!)
        } catch (e: FirebaseAuthException) {
            Result.failure(e)
        }
    }

    suspend fun insertResult(result: FavoriteResult) {
        dao.insert(result)
    }

    suspend fun deleteResult(result: FavoriteResult) {
        dao.delete(result)
    }

    suspend fun deleteByImageUri(imageUri: String): FavoriteResult? {
        return dao.deleteByImageUri(imageUri)
    }

    fun getResultByImageUri(imageUri: String): LiveData<FavoriteResult> {
        return dao.getResultByImageUri(imageUri)
    }

    suspend fun updateResult(result: FavoriteResult) {
        dao.update(result)
    }

    suspend fun getAllResult(): List<FavoriteResult> {
        return dao.getAllEvents()
    }

    suspend fun getFavoriteCount(): Int {
        return dao.getFavoriteCount()
    }

    suspend fun predictUpload(
        photo: MultipartBody.Part,
    ): utilResult<PredictUploadResponse> {
        return safeApiCall {
            apiService.predictUpload(photo)
        }
    }

    suspend fun getAllNews(
    ): utilResult<NewsResponse> {
        return safeApiCall {
            apiService.getAllNews()
        }
    }

    suspend fun getPredictHistories(
        uid: String
    ): utilResult<PredictHistoriesResponse> {
        return safeApiCall {
            apiService.getPredictHistories(uid)
        }
    }

    suspend fun sendContactUs(request: ContactUsRequest): utilResult<ContactUsResponse> {
        return safeApiCall {
            apiService.contactUs(request)
        }
    }

    companion object {

        @Volatile
        private var instance: Repository? = null

        fun getInstance(
            auth: FirebaseAuth,
            apiService: ApiService,
            dao: FavoriteResultDao
        ): Repository = instance ?: synchronized(this) {
            instance ?: Repository(auth, apiService, dao)
        }.also { instance = it }
    }
}
