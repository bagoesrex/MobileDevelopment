package com.example.skincure.ui.mainboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentMainBoardBinding

class MainBoard : Fragment() {

    private lateinit var binding : FragmentMainBoardBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainBoardBinding.inflate(inflater, container, false)

        binding.loginButton.setOnClickListener{
            findNavController().navigate(R.id.action_mainBoard_to_login)
        }

        binding.signupButton.setOnClickListener{
            findNavController().navigate(R.id.action_mainBoard_to_signUp)
        }

        return binding.root
    }

}