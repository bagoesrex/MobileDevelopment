package com.example.skincure.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.databinding.FragmentHistoryBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.isInternetAvailable
import com.example.skincure.utils.showToast
import com.google.firebase.auth.FirebaseAuth
import retrofit2.HttpException

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter
    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObserver()

        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            viewModel.getHistoriesPredict(uid)
        } else {
            Log.e("HistoryFragment", "User is not authenticated")
        }
    }

    private fun setupObserver() {
        binding.shimmerViewContainer.startShimmer()

        viewModel.history.observe(viewLifecycleOwner) { result ->
            historyAdapter.submitData(lifecycle, result)
        }

        historyAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                binding.shimmerViewContainer.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.GONE
                binding.emptyLayout.visibility = View.GONE
            } else if (loadState.refresh is LoadState.Error) {
                val error = (loadState.refresh as LoadState.Error).error
                val errorMessage = when (error) {
                    is HttpException -> {
                        val errorBody = error.response()?.errorBody()?.string()
                        errorBody ?: "API error"
                    }
                    else -> {
                        if (!isInternetAvailable(requireContext())) {
                            "No internet connection"
                        } else {
                            "Connection error"
                        }
                    }
                }
                Log.e("HistoryFragment", "Error: $errorMessage")
                binding.shimmerViewContainer.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.GONE
                binding.emptyLayout.visibility = View.VISIBLE
            } else {
                binding.shimmerViewContainer.visibility = View.GONE
                binding.historyRecyclerView.visibility = View.VISIBLE
                if (loadState.append.endOfPaginationReached && historyAdapter.itemCount == 0) {
                    binding.emptyLayout.visibility = View.VISIBLE
                } else {
                    binding.emptyLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter { data ->
            val bundle = Bundle().apply {
                putString(EXTRA_CAMERAX_IMAGE, data.imageUrl)
                putString(EXTRA_NAME, data.result)
                putString(EXTRA_DESCRIPTION, data.description)
                putString(EXTRA_DATE, data.createdAt)
                putString(EXTRA_SCORE, data.confidenceScore)
            }
            findNavController().navigate(R.id.action_home_to_resultDetail, bundle)
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
