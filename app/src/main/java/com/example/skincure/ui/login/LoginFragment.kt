package com.example.skincure.ui.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentLoginBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.createLoadingDialog
import com.example.skincure.utils.showToast
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
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

        setupListeners()
        observeLoginState()
        playAnimation()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString().trim()
            val password = binding.edLoginPassword.text.toString().trim()
            if (validateInput()) {
                viewModel.loginUser(email, password)
            }
        }

        binding.forgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }


        binding.googleLoginButton.setOnClickListener {
            signIn()
            val name = auth.currentUser?.displayName
            val userPreferences = UserPreferences(requireContext())
            userPreferences.saveNameSignUp(name.toString())
        }

        binding.registerButton.apply {
            text = Html.fromHtml(getString(R.string.no_account), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
            setOnClickListener {
                findNavController().navigate(R.id.action_login_to_signUp)
            }
        }
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
                    val errorMessage = state.message
                    showError(errorMessage)
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
                if (e.message?.contains("user cancelled the selector", true) == true) {
                    showError(getString(R.string.google_login_cancelled))
                } else {
                    showError(e.message.toString())
                }
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
                        showError(getString(R.string.error_invalid_google_id_token, e.message))
                    }
                } else {
                    showLoading(false)
                    showError(getString(R.string.error_invalid_credential_type))
                }
            }

            else -> {
                showLoading(false)
                showError(getString(R.string.error_unknown_credential_type))
            }
        }
    }


    private fun updateUI(currentUser: FirebaseUser?) {
        showLoading(false)
        if (currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userPreferences = UserPreferences(requireContext())
                    val idToken = task.result?.token
                    idToken?.let {
                        userPreferences.saveToken(it)
                        Log.d("token", it
                        )
                    }
                    if (currentUser.isEmailVerified) {
                        val displayName = currentUser.displayName ?: "User"
                        val welcomeMessage = getString(R.string.welcome_user, displayName)
                        showToast(requireContext(), welcomeMessage, Toast.LENGTH_SHORT)
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        sendVerificationEmail(currentUser)
                    }
                } else {
                    showError(getString(R.string.id_token_failed))
                }
            }
        } else {
            showError(getString(R.string.auth_failed))
        }
    }


    private fun sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                findNavController().navigate(R.id.action_login_to_otp)
            } else {
                val errorMessage = task.exception?.message.orEmpty()
                showError(getString(R.string.email_verification_failed, errorMessage))
            }
        }
    }

    private fun validateInput(): Boolean {
        binding.apply {
            return edLoginEmail.isValid() &&
                    edLoginPassword.isValid()
        }

    }

    @SuppressLint("MissingInflatedId")
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_forgot_password, null)
        builder.setView(view)
        val emailInput = view.findViewById<EditText>(R.id.edForgotEmail)
        builder.setPositiveButton(getString(R.string.send)) { dialog, _ ->
            val email = emailInput.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                sendPasswordResetEmail(email)
            } else {
                showError(getString(R.string.invalid_email))
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            showLoading(false)
            if (task.isSuccessful) {
                val successMessage = getString(R.string.password_reset_email_sent, email)
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_LONG).show()
            } else {
                val errorMessage = task.exception?.message.orEmpty()
                val failureMessage = getString(R.string.password_reset_email_failed, errorMessage)
                Toast.makeText(requireContext(), failureMessage, Toast.LENGTH_LONG).show()
            }
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
        showToast(requireContext(), message, Toast.LENGTH_LONG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.title, View.ALPHA, 1f).setDuration(500)
        val emailText = ObjectAnimator.ofFloat(binding.email, View.ALPHA, 1f).setDuration(500)
        val emailInput = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordText = ObjectAnimator.ofFloat(binding.password, View.ALPHA, 1f).setDuration(500)
        val passwordInput = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val forgotPassword = ObjectAnimator.ofFloat(binding.forgotPassword, View.ALPHA, 1f).setDuration(500)

        val inputFields = AnimatorSet().apply {
            playTogether(emailText, emailInput, passwordText, passwordInput, forgotPassword)
        }
        val together = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.loginButton, View.TRANSLATION_X, -300f, 0f).setDuration(500),
                ObjectAnimator.ofFloat(binding.googleLoginGroup, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.googleLoginGroup, View.TRANSLATION_X, 300f, 0f).setDuration(500),
                ObjectAnimator.ofFloat(binding.line, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.registerButton, View.TRANSLATION_Y, 300f, 0f).setDuration(500)

            )
        }
        AnimatorSet().apply {
            playSequentially(title, inputFields, together)
            start()
        }
    }

}