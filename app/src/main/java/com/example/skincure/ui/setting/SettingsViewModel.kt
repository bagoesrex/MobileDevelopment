package com.example.skincure.ui.setting

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    fun deleteAccount() {
        val user = auth.currentUser
        if (user != null) {
            _isDeleting.value = true
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            userRef.delete()
                .addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Log.d("SettingsViewModel", "User data successfully deleted from Firestore")
                        user.delete()
                            .addOnCompleteListener { task ->
                                _isDeleting.value = false
                                if (task.isSuccessful) {
                                    _deleteSuccess.value = true
                                } else {
                                    _errorMessage.value = task.exception?.message
                                    Log.d(
                                        "SettingsViewModel",
                                        "Failed to delete user: ${task.exception?.message}"
                                    )
                                }
                            }
                    } else {
                        _isDeleting.value = false
                        _errorMessage.value =
                            "Failed to delete user data from Firestore: ${deleteTask.exception?.message}"
                        Log.d(
                            "SettingsViewModel",
                            "Failed to delete user data from Firestore: ${deleteTask.exception?.message}"
                        )
                    }
                }
        } else {
            _errorMessage.value = "User not authenticated"
        }
    }

    fun logout() {
        viewModelScope.launch {

            _isLoggingOut.value = true
            try {
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