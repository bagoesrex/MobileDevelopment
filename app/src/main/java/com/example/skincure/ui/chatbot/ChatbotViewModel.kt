package com.example.skincure.ui.chatbot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.repository.Repository
import kotlinx.coroutines.launch

class ChatbotViewModel(private val repository: Repository) : ViewModel() {

    private var _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    fun getResponse(message: String) {
        viewModelScope.launch {
            try {
                val result = repository.getGenerativeResponse(message)
                _response.postValue(result)
            } catch (e: Exception) {
                Log.e("ChatbotViewModel", "Error: ${e.message}")
            }
        }
    }
}