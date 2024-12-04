package com.example.skincure.ui.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileViewModel() : ViewModel() {

    fun updateProfileImage(photoUri: Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(photoUri)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileViewModel", "Profile photo updated.")
                    onSuccess()
                } else {
                    onError()
                }
            }
    }

    fun updateProfileName(newName: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newName)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileViewModel", "Profile name updated.")
                    onSuccess()
                } else {
                    onError()
                }
            }
    }

    fun deleteProfileImage(onSuccess: () -> Unit, onError: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(null)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("ProfileViewModel", "Profile photo deleted.")
                    onSuccess()
                } else {
                    onError()
                }
            }
    }

}