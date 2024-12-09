package com.example.skincure.ui.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.data.remote.response.HistoryResponse
import com.example.skincure.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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
        // testing
        val historyList = listOf(
            HistoryResponse(
                id = "1",
                diseaseName = "Acne",
                timestamp = 1733770019417.toString(),
                description = "Kotlin has become one of the most popular programming languages for Android development.",
                imageUri = "https://www.clatsopcounty.gov/sites/g/files/vyhlif13571/files/styles/full_node_primary/public/media/public-health/image/12261/communicable_disease.jpg?itok=NV5L9DDw",
            ),
            HistoryResponse(
                id = "2",
                diseaseName = "Cacar",
                timestamp = 1733770015417.toString(),
                description = "Android 14 introduces several new features that enhance user experience and developer productivity.",
                imageUri = "https://www.verywellhealth.com/thmb/yI57XOKvdPi_bFAitEG2Pir1BSw=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/VirusIllustration-59ce8c1303f4020011702d0a.jpg",
            )
        )

        historyAdapter.submitList(historyList)
    }

    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter { history ->
            val bundle = Bundle().apply {
                putString(EXTRA_CAMERAX_IMAGE, history.imageUri)
                putString(EXTRA_NAME, history.diseaseName)
                putString(EXTRA_DESCRIPTION, history.description)
                putString(EXTRA_DATE, history.timestamp)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}