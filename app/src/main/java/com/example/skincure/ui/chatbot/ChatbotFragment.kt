package com.example.skincure.ui.chatbot

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.data.remote.response.ChatMessage
import com.example.skincure.databinding.FragmentChatbotBinding

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatbotViewModel by viewModels()
    private lateinit var adapter: ChatBotAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObserve()

        binding.sendButton.setOnClickListener {
            val userMessage = binding.messageEditText.text.toString()
            if (userMessage.isNotEmpty()) {
                val userChatMessage = ChatMessage(userMessage, true)
                adapter.submitList(adapter.currentList + userChatMessage)

                binding.messageEditText.text?.clear()

                viewModel.getResponse(userMessage)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatBotAdapter()
        binding.chatbotRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatbotRecyclerView.adapter = adapter
    }

    private fun setupObserve() {
        viewModel.response.observe(viewLifecycleOwner) { response ->
            val botChatMessage = ChatMessage(response, false)
            Log.d("userss", response)
            adapter.submitList(adapter.currentList + botChatMessage)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

