package com.example.skincure.ui.mainboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentMainBoardBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainBoardFragment : Fragment() {

    private var _binding: FragmentMainBoardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainBoardViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private lateinit var auth : FirebaseAuth
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBoardBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        userPreferences = UserPreferences(requireContext())

        setupView()
        return binding.root
    }

    private fun setupView() {
        binding.loginButton.setOnClickListener{
            findNavController().navigate(R.id.action_mainBoard_to_login)
        }

        binding.signupButton.setOnClickListener{
            findNavController().navigate(R.id.action_mainBoard_to_signUp)
        }
    }

    private fun checkUserStatus() {
        val token = userPreferences.getToken()
        val currentUser = auth.currentUser

        if (token != null && currentUser != null) {
            if (currentUser.isEmailVerified) {
                findNavController().navigate(R.id.action_mainBoard_to_home)
            } else {
                showEmailVerificationPrompt()
            }
        } else {
            Log.d("Auth", "Token atau pengguna tidak valid")
        }
    }

    private fun showEmailVerificationPrompt() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.verification_email_title))
            .setMessage(getString(R.string.verification_email_message))
            .setPositiveButton(getString(R.string.verification_email_positive_button)) { _, _ ->
                findNavController().navigate(R.id.action_mainBoard_to_otp)
            }
            .setNegativeButton(getString(R.string.verification_email_negative_button), null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        checkUserStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}