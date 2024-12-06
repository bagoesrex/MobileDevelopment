package com.example.skincure.ui.profile

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentProfileBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.createLoadingDialog
import com.example.skincure.utils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var userPreferences: UserPreferences
    private var loadingDialog: AlertDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        userPreferences = UserPreferences(requireContext())

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
                binding.toolbarId.toolbar.setNavigationOnClickListener {
                    binding.root.findNavController().popBackStack()
                }
            }
        }
        binding.buttonGaleri.setOnClickListener {
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding.deleteImage.setOnClickListener {
            deleteProfileImage()
        }
        binding.buttonSave.setOnClickListener {
            val name = binding.textInputEditTextName.text.toString()
            val age = binding.textInputEditTextAge.text.toString().toIntOrNull()
            if (name.isNotEmpty() && age != null) {
                showLoading(true)
                saveUserPreferences(name, age)
                updateProfileName(name)
            } else {
                showToast(getString(R.string.name_and_age_empty))
            }
        }
        playAnimation()
        showProfile()
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                updateProfileImage(uri)
            } else {
                Log.d(TAG, "Image not selected")
            }
        }

    private fun updateProfileImage(photoUri: Uri) {
        showLoading(true)
        viewModel.updateProfileImage(
            photoUri = photoUri,
            onSuccess = {
                showToast(getString(R.string.profile_image_update_success))
                binding.profileImage.setImageURI(photoUri)
                showLoading(false)
            },
            onError = {
                showLoading(false)
                showToast(getString(R.string.profile_image_update_failed))
            }
        )
    }

    private fun updateProfileName(newName: String) {
        viewModel.updateProfileName(
            newName = newName,
            onSuccess = {
                binding.userName.text = newName
                showLoading(false)
            },
            onError = {
                showLoading(false)
                showToast(getString(R.string.update_name_failed))
            }
        )
    }


    private fun deleteProfileImage() {
        showLoading(true)
        viewModel.deleteProfileImage(
            onSuccess = {
                showToast(getString(R.string.profile_image_delete_success))
                binding.profileImage.setImageResource(R.drawable.ic_person)
                showLoading(false)
            },
            onError = {
                showLoading(false)
                showToast(getString(R.string.profile_image_delete_failed))
            }
        )
    }

    private fun showProfile() {
        showLoading(true)
        val user = auth.currentUser
        val userAge = userPreferences.getUserAge()

        binding.textInputEditTextAge.setText(userAge.toString())
        user?.let {
            val displayName = it.displayName
            val photoUrl = it.photoUrl

            val userName = " ${displayName ?: "User"}"
            binding.userName.text = userName

            binding.textInputEditTextName.setText(displayName ?: "User")

            if (photoUrl != null) {
                Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .fit()
                    .centerCrop()
                    .into(binding.profileImage)
            }
        }
        showLoading(false)
    }

    private fun saveUserPreferences(name: String, age: Int) {
        val preferences = userPreferences
        preferences.saveUser(name, age)
        showToast(getString(R.string.data_saved))
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingDialog = loadingDialog ?: createLoadingDialog(requireContext())
            loadingDialog?.show()
        } else {
            loadingDialog?.dismiss()
        }
    }

    private fun showToast(message: String) {
        showToast(requireContext(), message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun playAnimation() {
        val profileImage = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.profileImage, View.SCALE_X, 0.5f, 1f)
                    .setDuration(500),
                ObjectAnimator.ofFloat(binding.profileImage, View.SCALE_Y, 0.5f, 1f)
                    .setDuration(500),
                ObjectAnimator.ofFloat(binding.profileImage, View.ALPHA, 0f, 1f)
                    .setDuration(500)
            )
        }
        val userName = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.userName, View.SCALE_X, 0.5f, 1f)
                    .setDuration(300),
                ObjectAnimator.ofFloat(binding.userName, View.SCALE_Y, 0.5f, 1f)
                    .setDuration(300),
                ObjectAnimator.ofFloat(binding.userName, View.ALPHA, 0f, 1f)
                    .setDuration(300)
            )
        }
        val buttonGaleri = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.buttonGaleri, View.TRANSLATION_X, 500f, 0f)
                    .setDuration(400),
                ObjectAnimator.ofFloat(binding.buttonGaleri, View.ALPHA, 0f, 1f)
                    .setDuration(400)
            )
        }
        val deleteImage = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.deleteImage, View.TRANSLATION_X, -500f, 0f)
                    .setDuration(400),
                ObjectAnimator.ofFloat(binding.deleteImage, View.ALPHA, 0f, 1f)
                    .setDuration(400)
            )
        }
        val textFields = AnimatorSet().apply {
            playSequentially(
                ObjectAnimator.ofFloat(binding.tvName, View.TRANSLATION_Y, 500f, 0f).apply {
                    duration = 100
                },
                ObjectAnimator.ofFloat(binding.tvName, View.ALPHA, 0f, 1f).apply {
                    duration = 100
                },
                ObjectAnimator.ofFloat(binding.tvUmur, View.TRANSLATION_Y, 500f, 0f).apply {
                    duration = 100
                },
                ObjectAnimator.ofFloat(binding.tvUmur, View.ALPHA, 0f, 1f).apply {
                    duration = 100
                },
                ObjectAnimator.ofFloat(binding.textInputEditTextName, View.TRANSLATION_Y, 500f, 0f)
                    .apply {
                        duration = 100
                    },
                ObjectAnimator.ofFloat(binding.textInputEditTextName, View.ALPHA, 0f, 1f).apply {
                    duration = 100
                },
                ObjectAnimator.ofFloat(binding.textInputEditTextAge, View.TRANSLATION_Y, 500f, 0f)
                    .apply {
                        duration = 100
                    },
                ObjectAnimator.ofFloat(binding.textInputEditTextAge, View.ALPHA, 0f, 1f).apply {
                    duration = 100
                }
            )
        }
        val saveButton = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.buttonSave, View.SCALE_X, 0.5f, 1f).apply {
                    duration = 500
                },
                ObjectAnimator.ofFloat(binding.buttonSave, View.SCALE_Y, 0.5f, 1f).apply {
                    duration = 500
                },
                ObjectAnimator.ofFloat(binding.buttonSave, View.ALPHA, 0f, 1f).apply {
                    duration = 500
                },
                ObjectAnimator.ofFloat(binding.buttonSave, View.TRANSLATION_Y, 500f, 0f).apply {
                    duration = 500
                }
            )
        }
        AnimatorSet().apply {
            playSequentially(
                profileImage,
                userName,
                buttonGaleri,
                deleteImage,
                textFields,
                saveButton
            )
            start()
        }
    }

}