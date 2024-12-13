package com.example.skincure.ui.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentOtpBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OtpViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        setupView()
        setupObservers()

        user?.let {
            viewModel.checkEmailVerification(it)
        }
    }

    private fun setupView() {
        binding.confirmButton.setOnClickListener {
            val user = auth.currentUser
            user?.let {
                viewModel.checkEmailVerification(it)
            }
        }

        binding.resendCodeButton.setOnClickListener {
            val user = auth.currentUser
            user?.let {
                viewModel.sendEmailVerification(it)
            }
        }
    }

    private fun setupObservers() {
        viewModel.emailVerified.observe(viewLifecycleOwner) { isVerified ->
            if (isVerified) {
                findNavController().navigate(R.id.action_otp_to_home)
            }
        }

        viewModel.verificationMessage.observe(viewLifecycleOwner) { message ->
            binding.textDescription.text = message
        }

        viewModel.canResendEmail.observe(viewLifecycleOwner) { canResend ->
            binding.resendCodeButton.isEnabled = canResend
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
