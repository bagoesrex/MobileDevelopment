package com.example.skincure.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val user: FirebaseUser) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun loginUser(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.loginWithEmailPassword(email, password)
                if (result.isSuccess) {
                    _loginState.value = LoginState.Success(result.getOrNull()!!)
                } else {
                    val exception = result.exceptionOrNull()
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Please check your email or password."
                        is FirebaseAuthInvalidUserException -> "User does not exist. Please check your email."
                        else -> exception?.message ?: "Unknown error"
                    }
                    _loginState.value = LoginState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }


    fun loginWithGoogle(idToken: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.authWithGoogle(idToken)
                if (result.isSuccess) {
                    _loginState.value = LoginState.Success(result.getOrNull()!!)
                } else {
                    _loginState.value = LoginState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
