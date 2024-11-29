package com.example.skincure.ui.signup

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.registerButton.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_signUp_to_otp)
            }
        }

        binding.googleRegisterButton.apply{
            setOnClickListener {
                // viewModel.signInWithGoogle(requireActivity())
                findNavController().navigate(R.id.action_signUp_to_home)
            }
        }

        binding.loginButton.apply {
            text = Html.fromHtml(getString(R.string.have_account), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                findNavController().navigate(R.id.action_signUp_to_login)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}