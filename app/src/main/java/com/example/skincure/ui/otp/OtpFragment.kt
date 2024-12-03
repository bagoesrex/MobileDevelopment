package com.example.skincure.ui.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentOtpBinding
import com.google.firebase.auth.FirebaseAuth

class OtpFragment : Fragment() {

    private var _binding: FragmentOtpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var userEmail: String
    private var emailVerified = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
                    Toast.makeText(requireContext(), "Email telah diverifikasi!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_otp_to_home)
                } else {
                    val verificationMessage = "Email verifikasi telah di kirim ke $userEmail"
                    binding.textDescription.text = verificationMessage
                    Toast.makeText(requireContext(), "Email $userEmail Belum Terverifikasi.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Gagal memverifikasi: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        if (user?.isEmailVerified == true) {
            Toast.makeText(requireContext(), "Email sudah terverifikasi!", Toast.LENGTH_SHORT).show()
        } else {
            user?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Email verifikasi telah dikirim ulang!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengirim email verifikasi: Silahkan tunggu beberapa saat dan coba lagi.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}