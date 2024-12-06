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
import com.squareup.picasso.Picasso

class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultDetailViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }
    private var isSaved: Boolean = false
    private lateinit var saveMenuItem: MenuItem

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentResultDetailBinding.inflate(inflater, container, false)
        setupView()
        return binding.root
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
        val score = arguments?.getFloat(EXTRA_SCORE) ?: "null score"
        val description = arguments?.getString(EXTRA_DESCRIPTION) ?: "null desc"

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
        binding.scorePredictionTextView.text = buildString{
            append("Score Prediciton:")
            append(score)
            append("%")
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

    private fun saveDataToRoom() {
        val imageUri = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        if (imageUri != null) {
            //testing
            val result = FavoriteResult(
                id = 0,
                imageUri = imageUri,
                diseaseName = getString(R.string.test_name),
                predictionScore = 75.2321F,
                description = getString(R.string.test_description)
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
        const val EXTRA_SCORE = "Score"
    }
}