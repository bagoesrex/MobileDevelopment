package com.example.skincure.ui.otp

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentOtpBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OtpViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private var emailVerified = false
    private var canResendEmail = true
    private var cooldownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val user = FirebaseAuth.getInstance().currentUser
        userEmail = user?.email.toString()
        setupListeners()

        checkEmailVerification()
        startCooldownTimer()
    }

    private fun setupListeners() {
        binding.confirmButton.setOnClickListener {
            checkEmailVerification()
        }

        binding.resendCodeButton.setOnClickListener {
            sendEmailVerification()
        }
    }

    private fun checkEmailVerification() {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                emailVerified = user.isEmailVerified
                if (emailVerified) {
                    context?.let {
                        Toast.makeText(
                            it,
                            getString(R.string.email_verified),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    findNavController().navigate(R.id.action_otp_to_home)
                } else {
                    val verificationMessage = getString(R.string.email_verification_sent, userEmail)
                    binding.textDescription.text = verificationMessage
                    context?.let {
                        Toast.makeText(
                            it,
                            getString(R.string.email_not_verified, userEmail),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                context?.let {
                    Toast.makeText(
                        it,
                        getString(R.string.verification_failed, task.exception?.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun sendEmailVerification() {
        val user: FirebaseUser? = auth.currentUser
        if (!canResendEmail) {
            context?.let {
                Toast.makeText(
                    it,
                    getString(R.string.wait_before_retry),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                context?.let {
                    Toast.makeText(
                        it,
                        getString(R.string.email_sent_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                startCooldownTimer()
            } else {
                context?.let {
                    Toast.makeText(
                        it,
                        getString(R.string.email_sent_failed, task.exception?.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun startCooldownTimer() {
        val cooldownTime = 60000L
        canResendEmail = false

        binding.resendCodeButton.isEnabled = false

        cooldownTimer = object : CountDownTimer(cooldownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.resendCodeButton.text = getString(R.string.wait_seconds, secondsLeft)
            }

            override fun onFinish() {
                canResendEmail = true
                binding.resendCodeButton.isEnabled = true
                binding.resendCodeButton.text = getString(R.string.resend_email)
            }
        }.start()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cooldownTimer?.cancel()
    }
}