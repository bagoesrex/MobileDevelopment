package com.example.skincure.ui.signup

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
import com.example.skincure.databinding.FragmentSignUpBinding
import com.example.skincure.utils.createLoadingDialog
import com.example.skincure.utils.showToast
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SignUpViewModel
    private lateinit var auth: FirebaseAuth
    private var loadingDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        viewModel = SignUpViewModel(AuthRepository(auth))

        setupView()
        observeSignUpState()
    }

    private fun observeSignUpState() {
        viewModel.signUpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignUpViewModel.SignUpState.Loading -> showLoading(true)

                is SignUpViewModel.SignUpState.Success -> {
                    showLoading(false)
                    val user = state.user
                    if (state.isGoogleSignIn) {
                        // Daftar dengan google
                        findNavController().navigate(R.id.action_signUp_to_home)
                    } else {
                        // Daftar dengan email dan password
                        user.sendEmailVerification().addOnCompleteListener { verifyTask ->
                            if (verifyTask.isSuccessful) {
                                updateUI(user, binding.edRegisterName.text.toString().trim())
                                if (!state.isGoogleSignIn) {
                                    findNavController().navigate(R.id.action_signUp_to_otp)
                                }
                            } else {
                                showToast(
                                    requireContext(),
                                    "Gagal mengirim email verifikasi.",
                                    Toast.LENGTH_SHORT
                                )
                            }
                        }
                    }
                }

                is SignUpViewModel.SignUpState.Error -> {
                    showLoading(false)
                    showToast(requireContext(), state.message, Toast.LENGTH_SHORT)
                    binding.edRegisterEmail.error = "Email sudah terdaftar"
                    binding.edRegisterEmail.requestFocus()
                    showError(state.message)
                }

                else -> showLoading(false)
            }
        }
    }


    private fun setupView() {
        binding.registerButton.apply {
            setOnClickListener {
                setOnClickListener {
                    val email = binding.edRegisterEmail.text.toString().trim()
                    val password = binding.edRegisterPassword.text.toString().trim()

                    if (!validateInput(email, password)) return@setOnClickListener

                    viewModel.signUpUser(email, password)
                }
            }
        }

        binding.googleRegisterButton.setOnClickListener {
            signUp()
        }

        binding.loginButton.apply {
            text = Html.fromHtml(getString(R.string.have_account), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                findNavController().navigate(R.id.action_signUp_to_login)
            }
        }
    }

    private fun signUp() {
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
                handleSignUp(result)
            } catch (e: GetCredentialException) {
                showLoading(false)
                Log.d("Error", e.message.toString())
            }
        }
    }

    private fun handleSignUp(result: GetCredentialResponse) {
        showLoading(true)
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.signUpWithGoogle(googleIdTokenCredential.idToken)
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

    private fun updateUI(user: FirebaseUser?, name: String) {
        if (name.isBlank()) {
            binding.edRegisterName.error = getString(R.string.name_required)
            binding.edRegisterName.requestFocus()
            return
        }

        user?.let {
            val profileUpdates = userProfileChangeRequest { displayName = name }
            it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Profile updated successfully with name: $name")
                } else {
                    Log.e(TAG, "Profile update failed: ${task.exception?.message}")
                }
            }
        }
        showLoading(false)
        if (user != null) {
            if (user.isEmailVerified) {
                showToast(
                    requireContext(),
                    "Selamat datang ${user.displayName ?: "User"}",
                    Toast.LENGTH_SHORT
                )
                findNavController().navigate(R.id.action_login_to_home)
            }
        } else {
            Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.edRegisterEmail.error = getString(R.string.invalid_email)
                binding.edRegisterEmail.requestFocus()
                false
            }

            password.length < 8 -> {
                binding.edRegisterPassword.error = getString(R.string.password_minimum_length)
                binding.edRegisterPassword.requestFocus()
                false
            }

            else -> true
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
        showToast(requireContext(), message)
    }

    companion object {
        private const val TAG = "SignUpFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}