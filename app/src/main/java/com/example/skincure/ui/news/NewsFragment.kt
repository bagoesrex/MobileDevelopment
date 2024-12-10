package com.example.skincure.ui.news

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.databinding.FragmentNewsBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory

class NewsFragment : Fragment() {

    private lateinit var newsAdapter: NewsAdapter
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewsViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

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
                binding.toolbarId.toolbar.setNavigationOnClickListener {
                    binding.root.findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        newsAdapter = NewsAdapter { news ->
            val bundle = Bundle().apply {
                putString(EXTRA_TITLE, news.name)
                putString(EXTRA_CAMERAX_IMAGE, news.image)
                putString(EXTRA_DESCRIPTION, news.description)
            }
            findNavController().navigate(R.id.action_news_to_newsDetail, bundle)
        }

        binding.newsRecyclerView.adapter = newsAdapter
    }

    private fun setupObserver() {
        binding.shimmerViewContainer.startShimmer()
        viewModel.newsResult.observe(viewLifecycleOwner) { result ->
            Handler(Looper.getMainLooper()).postDelayed({
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.newsRecyclerView.visibility = View.VISIBLE
                newsAdapter.submitList(result)
            }, 500)
            Log.d("NewsFragment", "News result: $result")
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            binding.shimmerViewContainer.stopShimmer()
            binding.shimmerViewContainer.visibility = View.GONE
            binding.newsRecyclerView.visibility = View.GONE
            Log.e("HistoryFragment", errorMessage)
        }
        viewModel.getAllNews()
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