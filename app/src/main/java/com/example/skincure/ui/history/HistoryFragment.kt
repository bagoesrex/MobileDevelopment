package com.example.skincure.ui.history

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.databinding.FragmentHistoryBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter
    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        setupView()
        setupRecyclerView()
        setupObserver()

        return binding.root
    }

    private fun setupView() {
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbarId.toolbar)
            supportActionBar?.apply {
                title = getString(R.string.history)
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_back)
                binding.toolbarId.toolbar.setNavigationOnClickListener {
                    binding.root.findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupObserver() {
        binding.shimmerViewContainer.startShimmer()
        viewModel.historyList.observe(viewLifecycleOwner) { favList ->
            favList.let {
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.shimmerViewContainer.stopShimmer()
                    binding.shimmerViewContainer.visibility = View.GONE
                    binding.historyRecyclerView.visibility = View.VISIBLE
                    historyAdapter.submitList(it)
                }, 500)
            }
            Log.d("HistoryFragment", "History List: $favList")
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage.let {
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.GONE
            }
            Log.e("HistoryFragment", errorMessage)
        }
        viewModel.fetchHistory()
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter { data ->
            val bundle = Bundle().apply {
                putString(EXTRA_CAMERAX_IMAGE, data["imageUri"] as? String)
                putString(EXTRA_NAME, data["diseaseName"] as? String)
                putString(EXTRA_DESCRIPTION, data["description"] as? String)
                putString(EXTRA_DATE, data["timestamp"] as? String)
                putString(EXTRA_SCORE, data["score"] as? String)
            }
            findNavController().navigate(R.id.action_history_to_resultDetail, bundle)
        }
        binding.historyRecyclerView.adapter = historyAdapter
    }

    companion object {
        const val EXTRA_CAMERAX_IMAGE = "CameraX Image"
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