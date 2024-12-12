package com.example.skincure.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentSettingsBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.createLoadingDialog
import com.example.skincure.utils.showConfirmationDialog
import com.example.skincure.utils.showToast

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupView()
        observeViewModel()
        return binding.root
    }

    private fun setupView() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.contactButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_contactUs)
        }

        binding.informationButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_information)
        }

        binding.logoutButton.setOnClickListener {
            showConfirmationDialog(
                requireContext(),
                getString(R.string.confirm_logout),
                onConfirm = {
                    viewModel.logout()
                    val userPreferences = UserPreferences(requireContext())
                    userPreferences.clearToken()
                },
                onCancel = {

                }
            )
        }

        binding.deleteUserButton.setOnClickListener {
            showConfirmationDialog(
                requireContext(),
                getString(R.string.confirm_delete_account),
                onConfirm = {
                    viewModel.deleteAccount()
                    val userPreferences = UserPreferences(requireContext())
                    userPreferences.clearToken()
                },
                onCancel = {}
            )
        }
    }

    private fun observeViewModel() {
        viewModel.isLoggingOut.observe(viewLifecycleOwner) { isLoggingOut ->
            showLoading(isLoggingOut)
        }
        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.action_settings_to_mainBoard)
            }
        }
        viewModel.deleteSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                showToast(requireContext(), getString(R.string.deleted_account), 3000)
                findNavController().navigate(R.id.action_settings_to_mainBoard)
            }
        }
        viewModel.isDeleting.observe(viewLifecycleOwner) { isDeleting ->
            showLoading(isDeleting)
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                showError(it)
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

    private fun showError(message: String) {
        showToast(requireContext(), message, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}