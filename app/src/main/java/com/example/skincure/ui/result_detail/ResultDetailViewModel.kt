package com.example.skincure.ui.result_detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.Result
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.remote.response.PredictUploadResponse
import com.example.skincure.data.repository.Repository
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


    fun deleteByImageUri(
        imageUri: String,
        onResult: (FavoriteResult?) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.deleteByImageUri(imageUri)
            onResult(result)
        }
    }
}