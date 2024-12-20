package com.example.skincure.ui.result_detail

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.data.Result
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentResultDetailBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.LoadImage
import com.example.skincure.utils.dateFormatter
import com.example.skincure.utils.reduceFileImage
import com.example.skincure.utils.showToast
import com.example.skincure.utils.uriToFile
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

    private var currentImageUri: Uri? = null

    private var imageUrl: String = ""
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
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.saveButton.setOnClickListener {
            if (!isSaved) {
                currentImageUri?.let { uri ->
                    saveDataToRoom(uri.toString())
                } ?: run {
                    Log.e(TAG, "No image URI available for saving.")
                }
                binding.saveButton.setImageResource(R.drawable.ic_save)
                isSaved = true
            } else {
                deleteDataFromRoom()

                binding.saveButton.setImageResource(R.drawable.ic_save_border)
                isSaved = false
            }
        }

        val imageUriString = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }

        currentImageUri = imageUri
        currentImageUri?.let {
            LoadImage.load(
                context = binding.root.context,
                imageView = binding.resultImageView,
                imageUrl = it.toString(),
                placeholder = R.color.placeholder,
            )
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
                    binding.saveButton.setImageResource(R.drawable.ic_save)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
//                    findNavController().navigate(R.id.action_resultDetail_to_home)
                    findNavController().navigateUp()
                }
            })
    }

    private fun observeData() {

        binding.saveButton.visibility = View.VISIBLE
        binding.resultLayout.visibility = View.VISIBLE
        binding.percentageCard.visibility = View.VISIBLE
        binding.nameCard.visibility = View.VISIBLE
        binding.dateCard.visibility = View.VISIBLE
        binding.pecentageTextView.visibility = View.VISIBLE
        binding.nameCard.visibility = View.VISIBLE
        binding.shimmerLayout.visibility = View.GONE
        binding.retryButton.visibility = View.GONE


        val percentageBar: View = binding.percentageBar

        val percentage = score.toDoubleOrNull() ?: 0.0

        percentageBar.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val parentWidth = (percentageBar.parent as View).width
                val width = (percentage * parentWidth / 100).toInt()
                val layoutParams = percentageBar.layoutParams as LinearLayout.LayoutParams
                layoutParams.width = width
                percentageBar.layoutParams = layoutParams
                percentageBar.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })


        val timestamp = timestampString
        val formattedDate = dateFormatter(timestamp)

        binding.nameTextView.text = name
        binding.timestampTextView.text = formattedDate


        val result = description.replaceFirst(Regex("(?s).*?(Penyebab:)"), "$1")

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
    }

    private fun observeViewModel() {
        viewModel.predictUploadResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Success -> {
                    val response = result.data

                    imageUrl = response.imageUrl
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

    private fun saveDataToRoom(imageUrl: String) {
        if (imageUrl != null) {
            val result = FavoriteResult(
                id = 0,
                imageUri = imageUrl,
                predictionScore = score.toDoubleOrNull() ?: 0.0,
                diseaseName = name,
                description = description,
                timestamp = timestampString
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