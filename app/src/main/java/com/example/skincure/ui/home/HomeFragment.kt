package com.example.skincure.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentHomeBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.ui.dashboard.DashboardFragment
import com.example.skincure.ui.favorite.FavoriteFragment
import com.example.skincure.ui.history.HistoryFragment
import com.example.skincure.ui.profile.ProfileFragment
import com.example.skincure.utils.isInternetAvailable
import com.example.skincure.utils.showToast

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(
            Injection.provideRepository(requireContext()),
        )
    }

    private var selectedFragmentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        if (!isInternetAvailable(requireContext())) {
            showToast(requireContext(), getString(R.string.no_internet))
            return binding.root
        }

        if (savedInstanceState != null) {
            selectedFragmentIndex = savedInstanceState.getInt("selectedFragmentIndex", 0)
        }

        binding.bottomNavigationView.selectedItemId = selectedFragmentIndex

        loadFragment(selectedFragmentIndex)
        setupView()

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedFragmentIndex", selectedFragmentIndex)
    }

    private fun setupView() {

        binding.cameraFab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_camera)
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    loadFragment(0)
                    true
                }

                R.id.history -> {
                    loadFragment(1)
                    true
                }

                R.id.favorite -> {
                    loadFragment(2)
                    true
                }

                R.id.profile -> {
                    loadFragment(3)
                    true
                }

                else -> false
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finishAffinity()
                }
            })
    }

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun loadFragment(index: Int) {
        val selectedFragment = when (index) {
            0 -> DashboardFragment()
            1 -> HistoryFragment()
            2 -> FavoriteFragment()
            3 -> ProfileFragment()
            else -> DashboardFragment()
        }
        replaceFragment(selectedFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}