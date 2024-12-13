package com.example.skincure.ui.signup

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.example.skincure.databinding.FragmentSignUpBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
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
    private val viewModel: SignUpViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
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

        setupView()
        observeSignUpState()

        playAnimation()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_signUp_to_mainBoard)
                }
            })
    }


    private fun observeSignUpState() {
        viewModel.signUpState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignUpViewModel.SignUpState.Loading -> showLoading(true)
                is SignUpViewModel.SignUpState.Success -> {
                    showLoading(false)
                    val user = state.user
                    if (state.isGoogleSignIn) {
                        val name = auth.currentUser?.displayName
                        val userPreferences = UserPreferences(requireContext())
                        userPreferences.saveNameSignUp(name.toString())
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
                                showError(getString(R.string.failed_send_verification_email))
                            }
                        }
                    }
                }

                is SignUpViewModel.SignUpState.Error -> {
                    showLoading(false)
                    binding.edRegisterEmail.error = getString(R.string.email_already_registered)
                    binding.edRegisterEmail.requestFocus()
                    showError(state.message)
                }

                else -> showLoading(false)
            }
        }
    }


    private fun setupView() {
        val password = binding.edRegisterPassword.text.toString()
        binding.edRegisterConfirmation.setPasswordToMatch(password)
        binding.registerButton.apply {
            setOnClickListener {
                val email = binding.edRegisterEmail.text.toString().trim()
                val password = binding.edRegisterPassword.text.toString().trim()
                if (validateInput()) {
                    viewModel.signUpUser(email, password)
                } else {
                    showError(getString(R.string.invalid_input))
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

    @SuppressLint("StringFormatInvalid")
    private fun updateUI(user: FirebaseUser?, name: String) {
        val token = user?.getIdToken(false)?.result?.token
        token?.let {
            val userPreferences = UserPreferences(requireContext())
            userPreferences.saveToken(it)
        }
        user?.let {
            val profileUpdates = userProfileChangeRequest { displayName = name }
            val userPreferences = UserPreferences(requireContext())
            userPreferences.saveNameSignUp(name)
            it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, getString(R.string.profile_updated_successfully, name))
                } else {
                    Log.e(TAG, getString(R.string.profile_update_failed, task.exception?.message))
                }
            }
        }
        showLoading(false)
        if (user != null) {
            if (user.isEmailVerified) {
                showToast(
                    requireContext(),
                    getString(
                        R.string.welcome_message,
                        user.displayName ?: getString(R.string.user)
                    ),
                    Toast.LENGTH_SHORT
                )
                findNavController().navigate(R.id.action_login_to_home)
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.authentication_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun validateInput(): Boolean {
        binding.apply {
            val password = edRegisterPassword.text.toString()
            edRegisterConfirmation.setPasswordToMatch(password)
            val validation = edRegisterConfirmation.text.toString() == password
            return edRegisterName.isValid()
                    && edRegisterEmail.isValid()
                    && edRegisterPassword.isValid()
                    &&
                    if (validation) {
                        return edRegisterConfirmation.error == null
                    } else {
                        edRegisterConfirmation.isValid()
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
        showToast(requireContext(), message)
    }

    companion object {
        private const val TAG = "SignUpFragment"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playAnimation() {
        val title = ObjectAnimator.ofFloat(binding.title, View.ALPHA, 1f).setDuration(500)
        val nameText = ObjectAnimator.ofFloat(binding.name, View.ALPHA, 1f).setDuration(500)
        val nameInput = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val emailText = ObjectAnimator.ofFloat(binding.email, View.ALPHA, 1f).setDuration(500)
        val emailInput = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordText = ObjectAnimator.ofFloat(binding.password, View.ALPHA, 1f).setDuration(500)
        val passwordInput = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val passwordConfirmText = ObjectAnimator.ofFloat(binding.confirmPassword, View.ALPHA, 1f).setDuration(500)
        val passwordConfirmInput = ObjectAnimator.ofFloat(binding.confirmationEditTextLayout, View.ALPHA, 1f).setDuration(500)
        val line = ObjectAnimator.ofFloat(binding.line, View.ALPHA, 1f).setDuration(500)

        val inputFields = AnimatorSet().apply {
            playTogether(nameText, nameInput, emailText, emailInput, passwordText, passwordInput,passwordConfirmText, passwordConfirmInput, line)
        }

        val together = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.loginButton, View.TRANSLATION_Y, 300f, 0f).setDuration(500),
                ObjectAnimator.ofFloat(binding.googleLoginGroup, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.googleLoginGroup, View.TRANSLATION_X, 300f, 0f).setDuration(500),
                ObjectAnimator.ofFloat(binding.registerButton, View.ALPHA, 1f).setDuration(500),
                ObjectAnimator.ofFloat(binding.registerButton, View.TRANSLATION_X, -300f, 0f).setDuration(500)

            )
        }

        AnimatorSet().apply {
            playSequentially(title, inputFields, together)
            start()
        }
    }
}