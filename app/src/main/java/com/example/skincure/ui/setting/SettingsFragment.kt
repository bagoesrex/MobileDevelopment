package com.example.skincure.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentSettingsBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.showConfirmationDialog
import com.example.skincure.utils.showDeleteDialog
import com.example.skincure.utils.showToast

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        observeViewModel()
    }

    private fun setupView() {
        binding.informationButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_information)
        }

        binding.contactButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_contactUs)
        }

        binding.logoutButton.setOnClickListener {
            showConfirmationDialog(
                requireContext(),
                getString(R.string.confirm_logout),
                onConfirm = {
                    viewModel.logout()
                },
                onCancel = {}
            )
        }

        binding.deleteUserButton.setOnClickListener {
            showDeleteDialog(
                requireContext(),
                getString(R.string.confirm_delete_account),
                onConfirm = {
                    viewModel.deleteAccount()
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
                showToast(requireContext(), it, 3000)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
