package com.example.skincure.ui.result_detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ResultDetailViewModel(private var repository: AuthRepository) : ViewModel() {

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