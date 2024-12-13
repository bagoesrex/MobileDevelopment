package com.example.skincure.ui.chatbot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.remote.response.ChatMessage
import com.example.skincure.data.repository.Repository
import kotlinx.coroutines.launch

class ChatbotViewModel(private val repository: Repository) : ViewModel() {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>()
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _currentMessage = MutableLiveData<String>()
    val currentMessage: LiveData<String> = _currentMessage

    init {
        _chatMessages.value = _chatMessages.value ?: emptyList()
    }

    fun sendMessage(message: String) {
        val updatedMessages = _chatMessages.value.orEmpty() + ChatMessage(message, true)
        _chatMessages.value = updatedMessages

        _currentMessage.value = ""

        getResponse(message)
    }

    private fun getResponse(message: String) {
        viewModelScope.launch {
            try {
                val result = repository.getGenerativeResponse(message)
                val updatedMessages = _chatMessages.value.orEmpty() + ChatMessage(result, false)
                _chatMessages.value = updatedMessages
            } catch (e: Exception) {
                Log.e("ChatbotViewModel", "Error: ${e.message}")
            }
        }
    }
}
