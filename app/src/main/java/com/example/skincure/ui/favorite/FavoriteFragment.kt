package com.example.skincure.ui.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.databinding.FragmentFavoriteBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory

class FavoriteFragment : Fragment() {

    private lateinit var favAdapter: FavoriteAdapter
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoriteViewModel by viewModels{
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        setupView()
        setupRecyclerView()
        setupObserver()

        return binding.root
    }

    private fun setupView() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarId.toolbar)
            supportActionBar?.apply {
                title = getString(R.string.favorite)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_back)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.favsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        favAdapter = FavoriteAdapter { fav ->
            val bundle = Bundle().apply {
                putString(EXTRA_CAMERAX_IMAGE, fav.imageUri)
                putString(EXTRA_NAME, fav.diseaseName)
                putString(EXTRA_DESCRIPTION, fav.description)
                fav.predictionScore?.let { putFloat(EXTRA_SCORE, it)
                }
            }
            findNavController().navigate(R.id.action_favorite_to_resultDetail, bundle)
        }
        binding.favsRecyclerView.adapter = favAdapter
    }

    private fun setupObserver() {
        // testing
        val favList = listOf(
            FavoriteResult(
                diseaseName = "Acne",
                predictionScore = 0.98f,
                description = "Kotlin has become one of the most popular programming languages for Android development.",
                imageUri = "https://static.vecteezy.com/system/resources/previews/033/662/051/non_2x/cartoon-lofi-young-manga-style-girl-while-listening-to-music-in-the-rain-ai-generative-photo.jpg",
            ),
            FavoriteResult(
                diseaseName = "Cacar",
                predictionScore = 0.98f,
                description = "Android 14 introduces several new features that enhance user experience and developer productivity.",
                imageUri = "https://cdn.pixabay.com/photo/2023/11/15/13/55/woman-8390124_640.jpg",
            )
        )

        favAdapter.submitList(favList)
    }

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
        const val EXTRA_NAME = "Name"
        const val EXTRA_DESCRIPTION = "Description"
        const val EXTRA_SCORE = "Score"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}