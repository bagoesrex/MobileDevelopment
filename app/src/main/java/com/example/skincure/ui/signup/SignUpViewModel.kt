package com.example.skincure.ui.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class SignUpViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _signUpState = MutableLiveData<SignUpState>()
    val signUpState: LiveData<SignUpState> get() = _signUpState

    sealed class SignUpState {
        object Loading : SignUpState()
        data class Success(val user: FirebaseUser, val isGoogleSignIn: Boolean) : SignUpState()
        data class Error(val message: String) : SignUpState()
    }


    fun signUpUser(email: String, password: String) {
        _signUpState.value = SignUpState.Loading
        viewModelScope.launch {
            val result = authRepository.signUpWithEmailPassword(email, password)
            if (result.isSuccess) {
                _signUpState.value = SignUpState.Success(result.getOrNull()!!, false)
            } else {
                _signUpState.value = SignUpState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun signUpWithGoogle(idToken: String) {
        _signUpState.value = SignUpState.Loading
        viewModelScope.launch {
            val result = authRepository.authWithGoogle(idToken)
            if (result.isSuccess) {
                _signUpState.value = SignUpState.Success(result.getOrNull()!!, true)
            } else {
                _signUpState.value = SignUpState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}