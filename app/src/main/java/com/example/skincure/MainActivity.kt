package com.example.skincure

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.ActivityMainBinding
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var auth : FirebaseAuth
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        userPreferences = UserPreferences(this)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_container_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }

    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            refreshAuthToken()
        }
    }

    private fun refreshAuthToken() {
        FirebaseAuth.getInstance().currentUser?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result?.token
                if (idToken != null) {
                    Log.d("HomeActivity", "Token refreshed: $idToken")
                    updateTokenInApp(idToken)
                }
            } else {
                Log.e("HomeActivity", "Failed to get updated token", task.exception)
            }
        }
    }

    private fun updateTokenInApp(token: String) {
        userPreferences.saveToken(token)
    }
}