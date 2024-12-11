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
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.skincure.R
import com.example.skincure.data.Result
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentResultDetailBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.dateFormatter
import com.example.skincure.utils.reduceFileImage
import com.example.skincure.utils.showToast
import com.example.skincure.utils.uriToFile
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultDetailViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private var isSaved: Boolean = false
    private lateinit var saveMenuItem: MenuItem
    private var isDataSaved = false

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

    @SuppressLint("UseCompatLoadingForDrawables")
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

        val imageUriFromArguments = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        imageUriFromArguments?.let {
            viewModel.getResultByImageUri(it).observe(viewLifecycleOwner) { result ->
                if (result != null) {
                    isSaved = true
                    saveMenuItem.icon = resources.getDrawable(R.drawable.ic_save, null)
                }
            }
        }
    }

    private fun observeData() {

        binding.resultLayout.visibility = View.VISIBLE
        binding.shimmerLayout.visibility = View.GONE
        binding.retryButton.visibility = View.GONE


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

                    showToast(requireContext(), getString(R.string.upload_failed))

                    binding.retryButton.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            uploadImage()
                            binding.shimmerLayout.visibility = View.VISIBLE
                            visibility = View.GONE
                        }
                    }
                }

                is Result.Loading -> {
                    binding.shimmerLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun uploadImage() {
        val userPreferences = UserPreferences(requireContext())
        val authToken = userPreferences.getToken()
        if (authToken != null) {
            currentImageUri?.let { uri ->
                val file = uriToFile(uri, requireActivity())
                val compressedFile = file.reduceFileImage()

                val photoRequestBody =
                    compressedFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val photoMultipart =
                    MultipartBody.Part.createFormData("file", compressedFile.name, photoRequestBody)

                viewModel.predictUpload(photoMultipart)
            } ?: run {
                Log.e(TAG, "currentImageUri is null. Cannot upload image.")
            }
        } else {
            showToast(requireContext(), "EXPIRED TOKEN")
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val diseaseName = name
        val description = description
        val timestamp = timestampString

        if (imageUri != null && userId != null) {

            val uri = Uri.parse(imageUri)
            if (isValidLocalUri(uri)) {
                viewModel.saveImageAndDataToFirestore(
                    imageUri = uri,
                    diseaseName = diseaseName,
                    description = description,
                    timestamp = timestamp,
                    userId = userId
                )
                viewModel.setImageUrl(uri.toString())
            } else {
                Log.e(TAG, "Invalid image URI: $imageUri")

            }
            viewModel.setImageUrl(uri.toString())
        } else {
            Log.e(TAG, "Image URI or User ID is null!")
        }
    }

    private fun isValidLocalUri(uri: Uri): Boolean {
        return uri.scheme == "content" || uri.scheme == "file"
    }

    private fun saveDataToRoom() {
        viewModel.imageUrl.observe(viewLifecycleOwner, Observer { imageUri ->
            if (imageUri != null) {
                val result = FavoriteResult(
                    id = 0,
                    imageUri = imageUri,
                    diseaseName = name,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )
                viewModel.insertResult(result)
            } else {
                Log.e(TAG, "image uri null.")
            }
        })
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