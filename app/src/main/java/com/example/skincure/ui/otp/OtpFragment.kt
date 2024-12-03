package com.example.skincure.ui.otp

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
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
                    Toast.makeText(
                        requireContext(),
                        "Email telah diverifikasi!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_otp_to_home)
                } else {
                    val verificationMessage = "Email verifikasi telah di kirim ke $userEmail"
                    binding.textDescription.text = verificationMessage
                    Toast.makeText(
                        requireContext(),
                        "Email $userEmail Belum Terverifikasi.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Gagal memverifikasi: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendEmailVerification() {
        val user: FirebaseUser? = auth.currentUser
        if (!canResendEmail) {
            Toast.makeText(
                requireContext(),
                "Tunggu beberapa saat sebelum mencoba lagi.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    "Email verifikasi telah dikirim.",
                    Toast.LENGTH_SHORT
                ).show()
                startCooldownTimer()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Gagal mengirim email: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                binding.resendCodeButton.text = buildString {
                    append("Tunggu ")
                    append(secondsLeft)
                    append(" detik")
                }
            }

            override fun onFinish() {
                canResendEmail = true
                binding.resendCodeButton.isEnabled = true
                binding.resendCodeButton.text = buildString {
                    append("Kirim Ulang Email Verifikasi")
                }
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cooldownTimer?.cancel()
    }
}