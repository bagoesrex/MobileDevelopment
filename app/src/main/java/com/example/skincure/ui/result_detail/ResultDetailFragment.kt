package com.example.skincure.ui.result_detail

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import com.example.skincure.R
import com.example.skincure.databinding.FragmentResultDetailBinding
import com.squareup.picasso.Picasso

class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultDetailViewModel by viewModels()

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
            }
        }

        val imageUriString = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }

        Log.d("ResultDetailFragment", "Image URI: $imageUriString")

        imageUri?.let {
            Picasso.get()
                .load(it)
                .placeholder(R.drawable.ic_gallery)
                .into(binding.resultImageView)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
    }
}