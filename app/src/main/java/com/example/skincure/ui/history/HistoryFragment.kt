package com.example.skincure.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.databinding.FragmentHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        userId?.let {
            // Ambil data dari Firestore collection users/{userId}/history
            db.collection("users").document(it).collection("history")
                .get()
                .addOnSuccessListener { documents ->
                    val favList = mutableListOf<Map<String, Any>>() // Menggunakan Map untuk data langsung

                    for (document in documents) {
                        val data = document.data // Ambil data langsung dari Firestore document
                        favList.add(data)
                    }

                    // Kirim data ke adapter
                    historyAdapter.submitList(favList)
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error getting documents", e)
                }
        } ?: run {
            Log.e("Firestore", "User not logged in!")
        }
    }


    private fun setupRecyclerView() {
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter { data ->
            val bundle = Bundle().apply {
                putString(EXTRA_CAMERAX_IMAGE, data["imageUri"] as? String)
                putString(EXTRA_NAME, data["diseaseName"] as? String)
                putString(EXTRA_DESCRIPTION, data["description"] as? String)
                putString(EXTRA_DATE, (data["timestamp"] as? Number).toString())
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