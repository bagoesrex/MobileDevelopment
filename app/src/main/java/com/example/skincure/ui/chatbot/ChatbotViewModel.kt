package com.example.skincure.ui.chatbot

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatbotViewModel : ViewModel() {

    private var _response = MutableLiveData<String>()
    val response: LiveData<String> = _response

    fun getResponse(message: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-pro-latest",
            apiKey = BuildConfig.apiKey
        )

        viewModelScope.launch {
            try {
                val inputContent = content() {
                    text(message)
                }

                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(inputContent)
                }

                Log.d("response", response.text.toString())
                _response.postValue(response.text)
            } catch (e: Exception) {
                Log.e("chatbot", "Error generating content: ${e.message}")
            }
        }
    }
}