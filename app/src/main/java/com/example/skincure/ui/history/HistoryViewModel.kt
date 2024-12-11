package com.example.skincure.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.Result
import com.example.skincure.data.remote.response.HistoriesItem
import com.example.skincure.data.repository.Repository
import kotlinx.coroutines.launch

class HistoryViewModel(private var repository: Repository) : ViewModel() {

    private val _historiesPredictResult = MutableLiveData<List<HistoriesItem>>()
    val historiesPredictResult: LiveData<List<HistoriesItem>> = _historiesPredictResult

    fun getHistoriesPredict(uid: String) {
        viewModelScope.launch {
            try {
                val result = repository.getPredictHistories(uid)

                when (result) {
                    is Result.Success -> {
                        _historiesPredictResult.value = result.data.histories
                    }
                    is Result.Error -> {
                        Log.e("HistoryViewModel", "Error")
                        _historiesPredictResult.value = emptyList()
                    }
                    Result.Loading -> TODO()
                }
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Unexpected error: ${e.message}")
                _historiesPredictResult.value = emptyList()
            }
        }
    }
}