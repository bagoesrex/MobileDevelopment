package com.example.skincure.data.repository

import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.local.FavoriteResultDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val dao: FavoriteResultDao
) {

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

    suspend fun updateResult(result: FavoriteResult) {
        dao.update(result)
    }

    suspend fun getAllResult(): List<FavoriteResult> {
        return dao.getAllEvents()
    }

    companion object {

        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(
            auth: FirebaseAuth,
            dao: FavoriteResultDao
        ): AuthRepository = instance ?: synchronized(this) {
            instance ?: AuthRepository(auth, dao)
        }.also { instance = it }
    }
}
