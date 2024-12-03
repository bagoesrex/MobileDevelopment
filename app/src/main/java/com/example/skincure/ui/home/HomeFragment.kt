package com.example.skincure.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentHomeBinding
import com.example.skincure.utils.createLoadingDialog
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupView()
        setupObserver()

        return binding.root
    }

    private fun setupView() {
        binding.settingButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        binding.buttonProfile.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    private fun setupObserver() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val pref = UserPreferences(requireContext())
                val displayName = user.displayName
                val namePref = pref.getUserName()
                val welcomeMessage = "Hi, ${displayName ?: namePref}"
                binding.userName.text = welcomeMessage

                val photoUrl = user.photoUrl
                Log.d("HomeFragment", "Photo URL: $photoUrl")
                if (photoUrl != null) {
                    Picasso.get()
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(binding.buttonProfile)
                } else {
                    binding.buttonProfile.setImageResource(R.drawable.ic_person)
                }
            } else {
                binding.userName.text = buildString {
                    append("Hi, Pengguna")
                }
                binding.buttonProfile.setImageResource(R.drawable.ic_person)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingDialog = loadingDialog ?: createLoadingDialog(requireContext())
            loadingDialog?.show()
        } else {
            loadingDialog?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}