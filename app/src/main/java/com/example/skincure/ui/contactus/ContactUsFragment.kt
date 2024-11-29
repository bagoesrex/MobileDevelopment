package com.example.skincure.ui.contactus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentContactUsBinding
import com.example.skincure.databinding.FragmentHomeBinding
import com.example.skincure.ui.home.HomeViewModel

class ContactUsFragment : Fragment() {

    private lateinit var binding: FragmentContactUsBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentContactUsBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {

    }
}