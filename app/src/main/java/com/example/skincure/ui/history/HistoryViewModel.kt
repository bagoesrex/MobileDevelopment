package com.example.skincure.ui.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryViewModel : ViewModel() {
    private val _historyList = MutableLiveData<List<Map<String, Any>>>()
    val historyList: LiveData<List<Map<String, Any>>> get() = _historyList

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchHistory() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        userId?.let {
            db.collection("users").document(it).collection("history")
                .get()
                .addOnSuccessListener { documents ->
                    val favList = mutableListOf<Map<String, Any>>()
                    for (document in documents) {
                        favList.add(document.data)
                    }
                    _historyList.value = favList
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error getting documents", e)
                    _error.value = "Failed to fetch history."
                }
        } ?: run {
            _error.value = "User not logged in!"
        }
    }
}