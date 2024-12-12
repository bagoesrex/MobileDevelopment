package com.example.skincure.ui.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skincure.R
import com.example.skincure.data.pref.UserPreferences
import com.example.skincure.databinding.FragmentDashboardBinding
import com.example.skincure.di.Injection
import com.example.skincure.ui.ViewModelFactory
import com.example.skincure.ui.favorite.FavoriteViewModel
import com.example.skincure.ui.history.HistoryViewModel
import com.example.skincure.ui.news.NewsAdapter
import com.example.skincure.ui.news.NewsFragment.Companion.EXTRA_CAMERAX_IMAGE
import com.example.skincure.ui.news.NewsFragment.Companion.EXTRA_DESCRIPTION
import com.example.skincure.ui.news.NewsFragment.Companion.EXTRA_TITLE
import com.example.skincure.ui.news.NewsViewModel
import com.example.skincure.utils.LoadImage
import com.google.firebase.auth.FirebaseAuth

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory(
            Injection.provideRepository(requireContext()),
        )
    }

    private lateinit var newsAdapter: NewsAdapter
    private val newsViewModel: NewsViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private val favViewModel: FavoriteViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(requireContext()))
    }

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupView()
        setupRecyclerView()
        setupObserver()
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            historyViewModel.getHistoriesPredict(uid)
        } else {
            Log.e("HistoryFragment", "User is not authenticated")
        }

        favViewModel.getFavoriteCount()
    }

    private fun setupView() {

        binding.profileButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }

        binding.newsLinkTextView.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_news)
        }

        binding.chatbotButton.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_chatbot)
        }

        binding.favoriteCard.setOnClickListener {
//                findNavController().navigate(R.id.action_home_to_favorite)
//            findNavController().navigate(R.id.action_home_to_history)
        }
    }

    private fun setupObserver() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                val displayName = user.displayName
                val welcomeMessage = buildString {
                    append("Hi, ")
                    append(displayName)
                }
                binding.usernameTextView.text = welcomeMessage
                val photoUrl = user.photoUrl
                if (photoUrl != null) {
                    LoadImage.load(
                        context = binding.root.context,
                        imageView = binding.profileButton,
                        imageUrl = photoUrl.toString(),
                        placeholder = R.color.placeholder,
                    )
                }
            } else {
                val pref = UserPreferences(requireContext())
                val namePref = pref.getUserName()
                if (namePref == null){
                    val welcomeMessage = "Hi, ${getString(R.string.welcome_text)}"
                    binding.usernameTextView.text = welcomeMessage
                }
                binding.profileButton.setImageResource(R.drawable.ic_person)
            }
        }

        historyViewModel.historiesCount.observe(viewLifecycleOwner) { count ->
            binding.historyTextView.text = count.toString()
        }

        historyViewModel.historyCount.observe(viewLifecycleOwner) { count ->
            binding.historyTextView.text = count.toString()
        }

        favViewModel.favoriteCount.observe(viewLifecycleOwner) { count ->
            binding.favoriteTextView.text = count.toString()
        }

        newsViewModel.newsResult.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                val latestNews = result.take(3)
                binding.newsRecyclerView.visibility = View.VISIBLE
                newsAdapter.submitList(latestNews)
            }
        }
//        newsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
//            binding.newsRecyclerView.visibility = View.GONE
//            Log.e("HistoryFragment", errorMessage)
//        }
        newsViewModel.getAllNews()
    }

    private fun setupRecyclerView() {
        binding.newsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        newsAdapter = NewsAdapter { news ->
            val bundle = Bundle().apply {
                putString(EXTRA_TITLE, news.name)
                putString(EXTRA_CAMERAX_IMAGE, news.image)
                putString(EXTRA_DESCRIPTION, news.description)
            }
            findNavController().navigate(R.id.action_home_to_newsDetail, bundle)
        }

        binding.newsRecyclerView.adapter = newsAdapter
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.shimmerLayout.visibility = View.VISIBLE
        } else {
            binding.shimmerLayout.visibility = View.GONE
            binding.tittleLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}