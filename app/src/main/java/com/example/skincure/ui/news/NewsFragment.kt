package com.example.skincure.ui.news

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
import com.example.skincure.data.remote.response.NewsResponse
import com.example.skincure.databinding.FragmentNewsBinding

class NewsFragment : Fragment() {

    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)

        setupView()
        setupRecyclerView()
        setupObserver()

        return binding.root
    }

    private fun setupView() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarId.toolbar)
            supportActionBar?.apply {
                title = getString(R.string.news)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_back)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        newsAdapter = NewsAdapter { fav ->
            val bundle = Bundle().apply {
                putString(EXTRA_TITLE, fav.title)
                putString(EXTRA_CAMERAX_IMAGE, fav.imageUrl)
                putString(EXTRA_DESCRIPTION, fav.description)
            }
            findNavController().navigate(R.id.action_news_to_newsDetail, bundle)
        }

        binding.newsRecyclerView.adapter = newsAdapter
    }

    private fun setupObserver() {
        // testing
        val newsList = listOf(
            NewsResponse(
                title = "Breaking News: Kotlin is Awesome!",
                description = "Kotlin has become one of the most popular programming languages for Android development.",
                imageUrl = "https://t4.ftcdn.net/jpg/05/62/02/41/360_F_562024161_tGM4lFlnO0OczLYHFFuNNdMUTG9ekHxb.jpg",
                createdAt = "2023-10-01T12:00:00Z"
            ),
            NewsResponse(
                title = "New Features in Android 14",
                description = "Android 14 introduces several new features that enhance user experience and developer productivity.",
                imageUrl = "https://t4.ftcdn.net/jpg/05/65/64/67/360_F_565646738_16aPBSad95Y3R75cHQtiwlJQ036oSUKW.jpg",
                createdAt = "2023-10-02T12:00:00Z"
            )
        )

        newsAdapter.submitList(newsList)
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