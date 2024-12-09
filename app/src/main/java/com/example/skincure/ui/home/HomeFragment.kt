package com.example.skincure.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentHomeBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.createLoadingDialog
import com.example.skincure.utils.isInternetAvailable
import com.example.skincure.utils.showToast
import com.squareup.picasso.Picasso

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(
            Injection.provideRepository(requireContext()),
        )
    }
    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupView()
        setupObserver()
        if (!isInternetAvailable(requireContext())) {
            showToast(requireContext(), getString(R.string.no_internet))
            return binding.root
        }
        return binding.root
    }

    private fun setupView() {
        binding.settingCard.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_home_to_settings)
            }
            binding.profileButton.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_profile)
            }

            binding.cameraCard.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_camera)
            }

            binding.newsCard.setOnClickListener {
                findNavController().navigate(R.id.action_home_to_news)
            }

            binding.favoriteCard.setOnClickListener {
//                findNavController().navigate(R.id.action_home_to_favorite)
                findNavController().navigate(R.id.action_home_to_history)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finishAffinity()
            }
        })
    }

    private fun setupObserver() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val displayName = user.displayName
                val welcomeMessage = buildString {
                    append("Hi, ")
                    append(displayName)
                }
                binding.usernameTextView.text = welcomeMessage
                val photoUrl = user.photoUrl
                if (photoUrl != null) {
                    Picasso.get()
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .fit()
                        .centerCrop()
                        .into(binding.profileButton)
                }
            } else {
                val pref = UserPreferences(requireContext())
                val namePref = pref.getUserName()
                val welcomeMessage = "Hi, ${namePref?.ifEmpty { getString(R.string.welcome_text) }}"
                binding.usernameTextView.text = welcomeMessage
                binding.profileButton.setImageResource(R.drawable.ic_person)
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