package com.example.skincure.ui.result_detail

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.skincure.R
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.databinding.FragmentResultDetailBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.DateUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultDetailViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private var isSaved: Boolean = false
    private lateinit var saveMenuItem: MenuItem
    private var isDataSaved = false
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResultDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        if (!isDataSaved) {
            saveDataToFirestore()
            isDataSaved = true
        }
    }

    private fun setupView() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarId.toolbar)
            supportActionBar?.apply {
                title = getString(R.string.result_detail)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_back)
                binding.toolbarId.toolbar.setNavigationOnClickListener {
                    binding.root.findNavController().popBackStack()
                }
            }
            setHasOptionsMenu(true)
        }

        val imageUriString = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }

        val name = arguments?.getString(EXTRA_NAME) ?: "null name"
        val description = arguments?.getString(EXTRA_DESCRIPTION) ?: "null desc"
        val timestampString = arguments?.getString(EXTRA_DATE) ?: "null date"
        val timestamp = timestampString.toLongOrNull() ?: 0L
        val formattedDate = DateUtils.formatTimestamp(timestamp)

        imageUri?.let {
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.ic_gallery)
                .into(binding.resultImageView)
        }

        binding.nameTextView.text = buildString {
            append("Hasil analsisis: ")
            append(name)
            append(" ")
        }

        binding.timestampTextView.text = buildString {
            append("Created At:")

            append(formattedDate)
        }
        binding.descriptionTextView.text = description

    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.appbar_menu, menu)
        saveMenuItem = menu.findItem(R.id.save_result).setOnMenuItemClickListener {
            if (!isSaved) {
                saveDataToRoom()

                saveMenuItem.icon = resources.getDrawable(R.drawable.ic_save, null)
                isSaved = true
            } else {
                deleteDataFromRoom()

                saveMenuItem.icon = resources.getDrawable(R.drawable.ic_save_border, null)
                isSaved = false
            }
            true
        }
    }

    private fun saveDataToFirestore() {
        val imageUri = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        if (imageUri != null) {
            val storageReference = FirebaseStorage.getInstance().reference
            val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")
            val userId = auth.currentUser?.uid
            userId?.let {
                val historyRef = db.collection("users").document(it).collection("history")
                historyRef.whereEqualTo("imageUri", imageUri)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            val uploadTask = imageRef.putFile(Uri.parse(imageUri))
                            uploadTask.addOnSuccessListener { taskSnapshot ->
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    val imageUrl = uri.toString()
                                    val resultData = mapOf(
                                        "imageUri" to imageUrl,
                                        "diseaseName" to getString(R.string.test_name),
                                        "description" to getString(R.string.test_description),
                                        "timestamp" to System.currentTimeMillis()
                                    )
                                    historyRef.add(resultData)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d("ResultDetailFragment", "Data saved to Firestore with ID: ${documentReference.id}")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("ResultDetailFragment", "Error saving data to Firestore", e)
                                        }
                                }
                            }
                        } else {
                            Log.d("ResultDetailFragment", "Data already exists in Firestore, skipping upload.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("ResultDetailFragment", "Error checking Firestore for existing image", e)
                    }
            } ?: run {
                Log.e("ResultDetailFragment", "User not logged in!")
            }
        }
    }

    private fun saveDataToRoom() {
        val imageUri = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        if (imageUri != null) {
            //testing
            val result = FavoriteResult(
                id = 0,
                imageUri = imageUri,
                diseaseName = getString(R.string.test_name),
                description = getString(R.string.test_description),
                timestamp = System.currentTimeMillis()
            )
            viewModel.insertResult(result)
        } else {
            Log.e(TAG, "image uri null.")
        }
    }

    private fun deleteDataFromRoom() {
        val imageUri = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        if (imageUri != null) {
            viewModel.deleteByImageUri(imageUri) { result ->
                if (result != null) {
                    viewModel.deleteResult(result)
                    Log.d(TAG, "kehapus.")
                } else {
                    Log.e(TAG, "not found")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        private const val TAG = "ResultDetailFragment"
        const val EXTRA_NAME = "Name"
        const val EXTRA_DESCRIPTION = "Description"
        const val EXTRA_DATE = "Date"
    }
}