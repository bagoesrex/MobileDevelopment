package com.example.skincure.ui.profile

import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.skincure.R
import com.example.skincure.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            updateProfile(uri)
        } else {
            Log.d(TAG, "Image not selected")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupView()

        return binding.root
    }

    private fun setupView() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarId.toolbar)
            supportActionBar?.apply {
                title = getString(R.string.profile)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_back)
            }
        }
        binding.buttonGaleri.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        showProfile()
    }

    private fun showProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val displayName = it.displayName
            val photoUrl = it.photoUrl

            val welcomeMessage = " ${displayName ?: "Pengguna"}"
            binding.userName.text = welcomeMessage

            if (photoUrl != null) {
                Picasso.get()
                    .load(photoUrl)
                    .into(binding.profileImage)
            }
        }
    }

    private fun updateProfile(photoUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(photoUri)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Profile photo updated.")
                    Toast.makeText(requireContext(), "Foto Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    binding.profileImage.setImageURI(photoUri)
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui foto profil", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}