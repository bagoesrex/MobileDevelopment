package com.example.skincure.ui.result_detail

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.Result
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.remote.response.PredictUploadResponse
import com.example.skincure.data.repository.Repository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ResultDetailViewModel(private var repository: Repository) : ViewModel() {

    private val _predictUploadResult = MutableLiveData<Result<PredictUploadResponse>>()
    val predictUploadResult: LiveData<Result<PredictUploadResponse>> get() = _predictUploadResult

    fun predictUpload(
        photo: MultipartBody.Part,
    ) {
        viewModelScope.launch {
            try {
                val result = repository.predictUpload(photo)
                _predictUploadResult.value = result
                Log.d("results", result.toString())
            } catch (e: Exception) {
                _predictUploadResult.value = Result.Error(e.message ?: "Unknown error occurred")
            } finally {
            }
        }
    }

    private val _result = MutableLiveData<FavoriteResult>()
    val result: LiveData<FavoriteResult> = _result

    fun insertResult(result: FavoriteResult) {
        viewModelScope.launch {
            repository.insertResult(result)
        }
    }

    fun deleteResult(result: FavoriteResult) {
        viewModelScope.launch {
            repository.deleteResult(result)
        }
    }

    fun getResultByImageUri(imageUri: String): LiveData<FavoriteResult?> {
        return repository.getResultByImageUri(imageUri)
    }

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    fun setImageUrl(url: String) {
        _imageUrl.value = url
    }

    fun deleteByImageUri(
        imageUri: String,
        onResult: (FavoriteResult?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.deleteByImageUri(imageUri)
            onResult(result)
        }
    }

    fun saveImageAndDataToFirestore(
        imageUri: Uri,
        diseaseName: String,
        description: String,
        timestamp: String,
        userId: String?,
    ) {
        viewModelScope.launch {
            try {
                val storageReference = FirebaseStorage.getInstance().reference
                val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")

                val uploadTask = imageRef.putFile(imageUri)
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        _imageUrl.postValue(imageUrl)

                        if (userId != null) {
                            saveDataToFirestore(
                                userId = userId,
                                imageUrl = imageUrl,
                                diseaseName = diseaseName,
                                description = description,
                                timestamp = timestamp
                            )
                        } else {
                            Log.e("ResultDetailViewModel", "User not logged in!")
                        }
                    }.addOnFailureListener { e ->
                        Log.e("ResultDetailViewModel", "Error getting download URL", e)
                    }
                }.addOnFailureListener { e ->
                    Log.e("ResultDetailViewModel", "Error uploading image to Firebase Storage", e)
                }
            } catch (e: Exception) {
                Log.e("ResultDetailViewModel", "Error saving data to Firestore", e)
            }
        }
    }

    private fun saveDataToFirestore(
        userId: String,
        imageUrl: String,
        diseaseName: String,
        description: String,
        timestamp: String,
    ) {
        viewModelScope.launch {
            try {
                val resultData = mapOf(
                    "imageUri" to imageUrl,
                    "diseaseName" to diseaseName,
                    "description" to description,
                    "timestamp" to timestamp
                )

                val historyRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("history")

                historyRef.add(resultData)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            "ResultDetailViewModel",
                            "Data saved to Firestore with ID: ${documentReference.id}"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e("ResultDetailViewModel", "Error saving data to Firestore", e)
                    }
            } catch (e: Exception) {
                Log.e("ResultDetailViewModel", "Firestore save error", e)
            }
        }
    }
}