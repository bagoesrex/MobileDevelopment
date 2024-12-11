package com.example.skincure.ui.contactus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.remote.response.ContactUsRequest
import com.example.skincure.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.skincure.data.Result
import com.example.skincure.data.remote.response.ContactUsResponse
import kotlinx.coroutines.launch

class ContactUsViewModel(private val repository: Repository) : ViewModel() {

    private val _contactUsResult = MutableStateFlow<Result<ContactUsResponse>>(Result.Loading)
    val contactUsResult: StateFlow<Result<ContactUsResponse>> get() = _contactUsResult

    fun sendContactUs(email: String, name: String, message: String) {
        val request = ContactUsRequest(email, name, message)
        _contactUsResult.value = Result.Loading

        viewModelScope.launch {
            when (val result = repository.sendContactUs(request)) {
                is Result.Success -> {
                    _contactUsResult.value = Result.Success(result.data)
                }
                is Result.Error -> {
                    _contactUsResult.value = Result.Error(result.error)
                }
                Result.Loading -> {
                }
            }
        }
    }
}