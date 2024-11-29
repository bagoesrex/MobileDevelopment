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

class MainBoardFragment : Fragment() {

    private var _binding: FragmentMainBoardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainBoardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBoardBinding.inflate(inflater, container, false)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}