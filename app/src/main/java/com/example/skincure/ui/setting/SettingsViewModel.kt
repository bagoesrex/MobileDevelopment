package com.example.skincure.ui.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.pref.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsViewModel(private val pref: UserPreferences) : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggingOut = MutableLiveData<Boolean>()
    val isLoggingOut: LiveData<Boolean> get() = _isLoggingOut

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> get() = _logoutSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun logout() {
        viewModelScope.launch {

            _isLoggingOut.value = true
            try {
                pref.clearToken()
                auth.signOut()
                delay(500)
                _logoutSuccess.value = true
                _errorMessage.value = null
            } catch (e: FirebaseAuthException) {
                _logoutSuccess.value = false
                _errorMessage.value = e.message
            } finally {
                _isLoggingOut.value = false
            }
            Log.d("SettingsViewModel", "Logout process completed")
        }
    }
}