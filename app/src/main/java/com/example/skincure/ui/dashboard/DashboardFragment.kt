package com.example.skincure.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentDashboardBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.squareup.picasso.Picasso

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(
            Injection.provideRepository(requireContext()),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        setupView()
        setupObserver()

        return binding.root
    }

    private fun setupView() {
        binding.settingCard.setOnClickListener {
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
//            findNavController().navigate(R.id.action_home_to_history)
        }
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
            binding.shimmerLayout.visibility = View.VISIBLE
        } else {
            binding.shimmerLayout.visibility = View.GONE
            binding.tittleLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}