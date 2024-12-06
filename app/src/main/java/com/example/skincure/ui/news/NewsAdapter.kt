package com.example.skincure.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.data.remote.response.NewsResponse
import com.example.skincure.databinding.NewsItemBinding
import com.squareup.picasso.Picasso

class NewsAdapter(
    private val onItemClick: (NewsResponse) -> Unit
) : ListAdapter<NewsResponse, NewsAdapter.NewsViewHolder>(NewsDiffCallBack()) {

    class NewsViewHolder(
        private val binding: NewsItemBinding,
        private val onItemClick: (NewsResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: NewsResponse) {
            binding.tittleTextView.text = news.title
            binding.descriptionTextView.text = news.description
            Picasso.get()
                .load(news.imageUrl)
                .into(binding.newsImageView)
            binding.dateTextView.text = news.createdAt

            binding.root.setOnClickListener {
                onItemClick(news)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = NewsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = getItem(position)
        holder.bind(news)
    }

    class NewsDiffCallBack : DiffUtil.ItemCallback<NewsResponse>() {
        override fun areItemsTheSame(oldItem: NewsResponse, newItem: NewsResponse): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: NewsResponse, newItem: NewsResponse): Boolean {
            return oldItem == newItem
        }
    }
}


