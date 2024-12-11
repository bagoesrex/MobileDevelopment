package com.example.skincure.ui.contactus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.skincure.data.Result
import com.example.skincure.R
import com.example.skincure.databinding.FragmentContactUsBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.utils.showToast

class ContactUsFragment : Fragment() {

    private var _binding: FragmentContactUsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContactUsViewModel by viewModels{
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
        setupObserver()
    }

    private fun setupView() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.submitButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val message = binding.messageEditText.text.toString()

            if (email.isNotEmpty() && name.isNotEmpty() && message.isNotEmpty()) {
                viewModel.sendContactUs(email, name, message)
            } else {
                showToast(requireContext(),"Please fill all fields")
            }
        }
    }

    private fun setupObserver() {
        lifecycleScope.launchWhenStarted {
            viewModel.contactUsResult.collect { result ->
                when (result) {
                    is Result.Loading -> {
                    }

                    is Result.Success -> {
                        showToast(requireContext(), "Message sent successfully!")
                    }

                    is Result.Error -> {
                        showToast(requireContext(), "Error: ${result.error}")
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}