package com.example.skincure.ui.setting

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentSettingsBinding

class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

//        toolbar
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.logoutButton.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_settings_to_mainBoard)
            }
        }

        return binding.root
    }
}