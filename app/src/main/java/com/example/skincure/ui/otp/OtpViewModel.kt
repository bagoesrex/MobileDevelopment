package com.example.skincure.ui.otp

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.skincure.data.repository.Repository
import com.google.firebase.auth.FirebaseUser

class OtpViewModel(private val repository: Repository) : ViewModel() {

    private val _emailVerified = MutableLiveData<Boolean>()
    val emailVerified: LiveData<Boolean> get() = _emailVerified

    private val _canResendEmail = MutableLiveData<Boolean>()
    val canResendEmail: LiveData<Boolean> get() = _canResendEmail

    private val _verificationMessage = MutableLiveData<String>()
    val verificationMessage: LiveData<String> get() = _verificationMessage

    private var cooldownTimer: CountDownTimer? = null

    fun checkEmailVerification(user: FirebaseUser) {
        repository.checkEmailVerification(user) { isVerified ->
            _emailVerified.value = isVerified
            if (isVerified) {
                _verificationMessage.value = "Email Verified"
            } else {
                _verificationMessage.value = "Please verify your email."
            }
        }
    }

    fun sendEmailVerification(user: FirebaseUser) {
        repository.sendEmailVerification(user)
        _verificationMessage.value = "Verification email sent"
        startCooldownTimer()
    }

    private fun startCooldownTimer() {
        _canResendEmail.value = false
        cooldownTimer = object : CountDownTimer(60000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _verificationMessage.value = "Please wait: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                _canResendEmail.value = true
                _verificationMessage.value = "You can resend the email now"
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        cooldownTimer?.cancel()
    }
}
