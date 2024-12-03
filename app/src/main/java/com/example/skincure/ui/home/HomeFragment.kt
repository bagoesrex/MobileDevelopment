package com.example.skincure.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }



    private fun setupView() {
//        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                requireActivity().finish()
//            }
//        })

        binding.settingButton.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_home_to_settings)
            }
        }

        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        val user = FirebaseAuth.getInstance().currentUser

        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updatedUser = FirebaseAuth.getInstance().currentUser
                val displayName = updatedUser?.displayName
                val welcomeMessage = "Selamat datang ${displayName ?: "Pengguna"}"
                binding.userName.text = welcomeMessage
            } else {
                binding.userName.text = getString(R.string.user_name_not_loaded)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}