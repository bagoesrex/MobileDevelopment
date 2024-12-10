package com.example.skincure.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.data.remote.response.NewsResponseItem
import com.example.skincure.databinding.NewsItemBinding
import com.example.skincure.utils.dateFormatter
import com.squareup.picasso.Picasso

class NewsAdapter(
    private val onItemClick: (NewsResponseItem) -> Unit
) : ListAdapter<NewsResponseItem, NewsAdapter.NewsViewHolder>(NewsDiffCallBack()) {

    class NewsViewHolder(
        private val binding: NewsItemBinding,
        private val onItemClick: (NewsResponseItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(news: NewsResponseItem) {
            binding.tittleTextView.text = news.name
            binding.descriptionTextView.text = news.description
            Picasso.get()
                .load(news.image)
                .into(binding.newsImageView)
            binding.dateTextView.text = dateFormatter(news.createdAt)

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

    class NewsDiffCallBack : DiffUtil.ItemCallback<NewsResponseItem>() {
        override fun areItemsTheSame(oldItem: NewsResponseItem, newItem: NewsResponseItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsResponseItem, newItem: NewsResponseItem): Boolean {
            return oldItem == newItem
        }
    }
}


