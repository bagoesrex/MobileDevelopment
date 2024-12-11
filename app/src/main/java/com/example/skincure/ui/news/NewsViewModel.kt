package com.example.skincure.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.repository.Repository
import com.example.skincure.data.Result
import com.example.skincure.data.remote.response.NewsResponseItem
import kotlinx.coroutines.launch

class NewsViewModel(private val repository: Repository) : ViewModel() {

    private val _newsResult = MutableLiveData<List<NewsResponseItem>>()
    val newsResult: LiveData<List<NewsResponseItem>> = _newsResult

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getAllNews() {
        viewModelScope.launch {
            when (val result = repository.getAllNews()) {
                is Result.Success -> {
                    _newsResult.value = result.data
                }
                is Result.Error -> {
                    _newsResult.value = emptyList()
                    _error.value = result.error
                }
                Result.Loading -> TODO()
            }
        }
    }
}
