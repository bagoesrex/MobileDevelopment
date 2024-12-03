package com.example.skincure.ui.login

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.repository.AuthRepository
import com.example.skincure.databinding.FragmentLoginBinding
import com.example.skincure.utils.createLoadingDialog
import com.example.skincure.utils.showToast
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LoginViewModel
    private lateinit var auth: FirebaseAuth
    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val authRepository = AuthRepository(auth)

        viewModel = LoginViewModel(authRepository)
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.loginUser(email, password)
            }
        }

        binding.googleLoginButton.setOnClickListener {
            signIn()
        }

        binding.registerButton.apply {
            text = Html.fromHtml(getString(R.string.no_account), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                findNavController().navigate(R.id.action_login_to_signUp)
            }
        }
        observeLoginState()
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> showLoading(true)
                is LoginViewModel.LoginState.Success -> {
                    showLoading(false)
                    updateUI(state.user)
                }
                is LoginViewModel.LoginState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
            }
        }
    }


    private fun signIn() {
        val credentialManager = CredentialManager.create(requireContext())
        val googleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.your_web_client_id)).build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        showLoading(true)
        lifecycleScope.launch {
            try {
                val result: GetCredentialResponse = credentialManager.getCredential(
                    request = request,
                    context = requireContext(),
                )
                handleSignIn(result)
            } catch (e: GetCredentialException) {
                showLoading(false)
                Log.d("Error", e.message.toString())
            }
        }
    }

    private fun handleSignIn(result: GetCredentialResponse) {
        showLoading(true)
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                    } catch (e: GoogleIdTokenParsingException) {
                        showLoading(false)
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    showLoading(false)
                    Log.e(TAG, "Kredensial tidak valid")
                }
            }

            else -> {
                showLoading(false)
                Log.e(TAG, "Kredensial tidak valid")
            }
        }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        showLoading(false)
        if (currentUser != null) {
            if (currentUser.isEmailVerified) {
                showToast(
                    requireContext(),
                    "Selamat datang ${currentUser.displayName ?: "User"}",
                    Toast.LENGTH_SHORT
                )
                findNavController().navigate(R.id.action_login_to_home)
            } else {
                sendVerificationEmail(currentUser)
            }
        } else {
            Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                findNavController().navigate(R.id.action_login_to_otp)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Gagal mengirim email verifikasi: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val TAG = "LoginFragment"
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.edLoginEmail.error = "Email tidak valid"
                binding.edLoginEmail.requestFocus()
                false
            }

            password.isEmpty() || password.length < 8 -> {
                binding.edLoginPassword.error = "Password minimal 8 karakter"
                binding.edLoginPassword.requestFocus()
                false
            }

            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            if (loadingDialog == null) {
                loadingDialog = createLoadingDialog(requireContext())
            }
            loadingDialog?.apply {
                if (!isShowing) show()
            }
        } else {
            loadingDialog?.apply {
                if (isShowing) dismiss()
            }
            loadingDialog = null
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}