package com.example.skincure.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.repository.Repository
import kotlinx.coroutines.launch

class FavoriteViewModel(private val repository: Repository) : ViewModel() {
    private val _favoriteList = MutableLiveData<List<FavoriteResult>>()
    val favoriteList: LiveData<List<FavoriteResult>> = _favoriteList

    fun getAllFavorite() {
        viewModelScope.launch {
            val results = repository.getAllResult()
            _favoriteList.postValue(results)
        }
    }
}
