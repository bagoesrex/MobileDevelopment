package com.example.skincure.ui.news_detail

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.skincure.R
import com.example.skincure.databinding.FragmentNewsDetailBinding
import com.example.skincure.utils.LoadImage

class NewsDetailFragment : Fragment() {

    private var _binding: FragmentNewsDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    private fun setupView() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val imageUriString = arguments?.getString(EXTRA_CAMERAX_IMAGE)
        val imageUri: Uri? = imageUriString?.let { Uri.parse(it) }

        val tittle = arguments?.getString(EXTRA_TITLE)
        val description = arguments?.getString(EXTRA_DESCRIPTION)

        binding.tittleTextView.text = tittle
        binding.descriptionTextView.text = description

        imageUri?.let {
            LoadImage.load(
                context = binding.root.context,
                imageView = binding.newsImageView,
                imageUrl = it.toString(),
                placeholder = R.color.placeholder,
            )
        }
    }

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val EXTRA_TITLE = "Title"
        const val EXTRA_DESCRIPTION = "Description"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}