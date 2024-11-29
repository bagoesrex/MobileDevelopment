package com.example.skincure.ui.login

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
import com.example.skincure.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        binding.loginButton.apply {
            setOnClickListener {
                findNavController().navigate(R.id.action_login_to_otp)
            }
        }

        binding.googleLoginButton.apply{
            setOnClickListener {
//                viewModel.signInWithGoogle(requireActivity())
                findNavController().navigate(R.id.action_login_to_home)
            }
        }

        binding.registerButton.apply {
            text = Html.fromHtml(getString(R.string.no_account), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                findNavController().navigate(R.id.action_login_to_signUp)
            }
        }
    }
}