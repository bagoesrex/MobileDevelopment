package com.example.skincure.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.skincure.data.repository.Repository
import com.google.firebase.auth.FirebaseUser

class DashboardViewModel(private val repository: Repository) : ViewModel() {
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        fetchUser()
    }

    private fun fetchUser() {
        _loading.value = true
        val currentUser = repository.getCurrentUser()

        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _user.value = currentUser
            } else {
                _user.value = null
            }
            _loading.value = false
        }
    }
}