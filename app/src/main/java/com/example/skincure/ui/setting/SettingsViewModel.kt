package com.example.skincure.ui.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skincure.data.repository.Repository
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsRepository: Repository) : ViewModel() {

    private val _isLoggingOut = MutableLiveData<Boolean>()
    val isLoggingOut: LiveData<Boolean> get() = _isLoggingOut

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> get() = _logoutSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _isDeleting = MutableLiveData<Boolean>()
    val isDeleting: LiveData<Boolean> get() = _isDeleting

    private val _deleteSuccess = MutableLiveData<Boolean>()
    val deleteSuccess: LiveData<Boolean> get() = _deleteSuccess

    fun logout() {
        viewModelScope.launch {
            _isLoggingOut.value = true
            try {
                val success = settingsRepository.logout()
                _logoutSuccess.value = success
                _errorMessage.value = if (success) null else "Logout failed"
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _logoutSuccess.value = false
            } finally {
                _isLoggingOut.value = false
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _isDeleting.value = true
            try {
                val success = settingsRepository.deleteAccount()
                _deleteSuccess.value = success
                _errorMessage.value = if (success) null else "Account deletion failed"
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _deleteSuccess.value = false
            } finally {
                _isDeleting.value = false
            }
        }
    }
}
