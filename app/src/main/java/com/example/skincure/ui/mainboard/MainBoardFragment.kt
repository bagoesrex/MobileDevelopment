package com.example.skincure.ui.mainboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentMainBoardBinding
import com.example.skincure.ui.login.LoginViewModel

class MainBoardFragment : Fragment() {

    private lateinit var binding : FragmentMainBoardBinding
    private val viewModel: MainBoardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBoardBinding.inflate(inflater, container, false)

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
}