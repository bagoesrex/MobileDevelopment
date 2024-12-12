package com.example.skincure.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentHomeBinding
import com.example.skincure.ui.dashboard.DashboardFragment
import com.example.skincure.ui.favorite.FavoriteFragment
import com.example.skincure.ui.history.HistoryFragment
import com.example.skincure.ui.setting.SettingsFragment
import com.example.skincure.utils.isInternetAvailable
import com.example.skincure.utils.showToast

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var selectedFragmentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isInternetAvailable(requireContext())) {
            showToast(requireContext(), getString(R.string.no_internet))
        }

        if (savedInstanceState != null) {
            selectedFragmentIndex = savedInstanceState.getInt("selectedFragmentIndex", 0)
        }

        binding.bottomNavigationView.selectedItemId = when (selectedFragmentIndex) {
            0 -> R.id.home
            1 -> R.id.history
            2 -> R.id.favorite
            3 -> R.id.profile
            else -> R.id.home
        }

        loadFragment(selectedFragmentIndex)
        setupView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("selectedFragmentIndex", selectedFragmentIndex)
    }

    private fun setupView() {
        binding.cameraFab.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_camera)
        }

        binding.apply{
            bottomNavigationView.itemIconTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            bottomNavigationView.itemTextColor = ContextCompat.getColorStateList(requireContext(), R.color.white)
        }
        binding.bottomNavigationView.setOnItemSelectedListener { item ->

            if (item.itemId == binding.bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener false
            }

            when (item.itemId) {
                R.id.home -> {
                    selectedFragmentIndex = 0
                    loadFragment(selectedFragmentIndex)
                    true
                }

                R.id.history -> {
                    selectedFragmentIndex = 1
                    loadFragment(selectedFragmentIndex)
                    true
                }

                R.id.favorite -> {
                    selectedFragmentIndex = 2
                    loadFragment(selectedFragmentIndex)
                    true
                }

                R.id.profile -> {
                    selectedFragmentIndex = 3
                    loadFragment(selectedFragmentIndex)
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
            3 -> SettingsFragment()
            else -> DashboardFragment()
        }
        replaceFragment(selectedFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}