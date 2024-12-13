package com.example.skincure.ui.chatbot

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.databinding.FragmentChatbotBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatbotViewModel by viewModels() {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private lateinit var adapter: ChatBotAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var keyboardListener: ViewTreeObserver.OnGlobalLayoutListener

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
        setupKeyboardListener()
        setupObserve()

        viewModel.currentMessage.observe(viewLifecycleOwner) { message ->
            binding.messageEditText.setText(message)
        }

        binding.sendButton.setOnClickListener {
            val userMessage = binding.messageEditText.text.toString()
            if (userMessage.isNotEmpty()) {
                viewModel.sendMessage(userMessage)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatBotAdapter()
        layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        binding.chatbotRecyclerView.layoutManager = layoutManager
        binding.chatbotRecyclerView.adapter = adapter
    }

    private fun setupObserve() {
        viewModel.chatMessages.observe(viewLifecycleOwner) { chatMessages ->
            adapter.submitList(chatMessages)

            binding.chatbotRecyclerView.postDelayed({
                binding.chatbotRecyclerView.scrollToPosition(chatMessages.size)
            }, 100)
        }
    }

    private fun setupKeyboardListener() {
        keyboardListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (_binding == null) return

                val rect = Rect()
                binding.root.getWindowVisibleDisplayFrame(rect)
                val screenHeight = binding.root.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                if (keypadHeight > screenHeight * 0.15) {
                    binding.chatbotRecyclerView.postDelayed({
                        binding.chatbotRecyclerView.scrollToPosition(adapter.currentList.size - 1)
                    }, 100)
                }
            }
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(keyboardListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener)
        binding.chatbotRecyclerView.adapter = null
        _binding = null
    }
}
