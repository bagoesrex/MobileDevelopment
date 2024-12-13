package com.example.skincure.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.skincure.data.repository.Repository

class ProfileViewModel(private val repository: Repository) : ViewModel() {

    fun updateProfileImage(photoUri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.updateProfileImage(photoUri, onSuccess, onError)
    }

    fun updateProfileName(newName: String, onSuccess: () -> Unit, onError: () -> Unit) {
        repository.updateProfileName(newName, onSuccess, onError)
    }

    fun deleteProfileImage(onSuccess: () -> Unit, onError: () -> Unit) {
        repository.deleteProfileImage(onSuccess, onError)
    }
}
