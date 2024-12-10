package com.example.skincure.ui.result_detail

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
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
import com.example.skincure.utils.reduceFileImage
import com.example.skincure.utils.uriToFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.example.skincure.data.Result
import com.example.skincure.utils.dateFormatter

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

    private var currentImageUri: Uri? = null

    private var name: String = ""
    private var description: String = ""
    private var timestampString: String = ""
    private var score: String = ""

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

        if (name.isNotEmpty() && description.isNotEmpty()) {
            observeData()

        } else {
            uploadImage()
            observeViewModel()
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

        currentImageUri = imageUri
        currentImageUri?.let {
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.ic_gallery)
                .into(binding.resultImageView)
        }

        name = arguments?.getString(EXTRA_NAME) ?: name
        description = arguments?.getString(EXTRA_DESCRIPTION) ?: description
        score = arguments?.getString(EXTRA_SCORE) ?: score
        timestampString = arguments?.getString(EXTRA_DATE) ?: timestampString


    }

    private fun observeData() {
        val timestamp = timestampString
        val formattedDate = dateFormatter(timestamp)

        binding.nameTextView.text = buildString {
            append("Hasil analysis: ")
            append(name)
        }
        binding.scorePredictionTextView.text = buildString {
            append("Prediction Score: ")
            val score = score.toDoubleOrNull() ?: 0.0
            append(score.toInt())
            append("%")
        }
        binding.timestampTextView.text = buildString {
            append("Created At: ")
            append(formattedDate)
        }

        val result = description.replace(Regex("(Penyebab:|Pencegahan:|Pengobatan:|Penjelasan:)"), "\n$1")
        val spannableString = SpannableString(result)
        val regex = Regex("(Kondisi:|Penyebab:|Pencegahan:|Pengobatan:|Penjelasan:)")
        val matches = regex.findAll(result)

        for (match in matches) {
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                match.range.first,
                match.range.last + 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.descriptionTextView.text = spannableString

        if (!isDataSaved) {
            saveDataToFirestore()
            isDataSaved = true
        }
    }

    private fun observeViewModel() {
        viewModel.predictUploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    val response = result.data

                    name = response.result
                    description = response.description
                    score = response.score.toString()
                    timestampString = response.createdAt
                    observeData()
                }
                is Result.Error -> {
                    Log.e(TAG, "Upload failed: ${result.error}")
                }
                is Result.Loading -> {
                }
            }
        }
    }

    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val file = uriToFile(uri, requireActivity())
            val compressedFile = file.reduceFileImage()

            val photoRequestBody = compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val photoMultipart =
                MultipartBody.Part.createFormData("file", compressedFile.name, photoRequestBody)

            viewModel.predictUpload(photoMultipart)
        } ?: run {
            Log.e(TAG, "currentImageUri is null. Cannot upload image.")
        }

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
            val uploadTask = imageRef.putFile(Uri.parse(imageUri))

            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val userId = auth.currentUser?.uid
                    val diseaseName = name
                    val description = description
                    val timestamp = timestampString
                    val score = score

                    val resultData = mapOf(
                        "imageUri" to imageUrl,
                        "diseaseName" to diseaseName,
                        "description" to description,
                        "timestamp" to timestamp,
                        "score" to score
                    )

                    userId?.let {
                        val historyRef = db.collection("users").document(it).collection("history")
                        historyRef.whereEqualTo("imageUri", imageUri)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    historyRef.add(resultData)
                                        .addOnSuccessListener { documentReference ->
                                            Log.d(
                                                TAG,
                                                "Data saved to Firestore with ID: ${documentReference.id}"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error saving data to Firestore", e)
                                        }
                                } else {
                                    Log.d(TAG, "Data already exists, not adding again.")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error checking data existence in Firestore", e)
                            }
                    } ?: run {
                        Log.e(TAG, "User not logged in!")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Error uploading image to Firestore", e)
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

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        private const val TAG = "ResultDetailFragment"
        const val EXTRA_NAME = "Name"
        const val EXTRA_DESCRIPTION = "Description"
        const val EXTRA_DATE = "Date"
        const val EXTRA_SCORE = "Score"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}