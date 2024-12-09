package com.example.skincure.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.data.remote.response.HistoryResponse
import com.example.skincure.databinding.HistoryItemBinding
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val onItemClick: (HistoryResponse) -> Unit
) : ListAdapter<HistoryResponse, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallBack()) {

    class HistoryViewHolder(
        private val binding: HistoryItemBinding,
        private val onItemClick: (HistoryResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(history: HistoryResponse) {
            binding.tittleTextView.text = history.diseaseName
            binding.descriptionTextView.text = history.description
            Picasso.get()
                .load(history.imageUri)
                .into(binding.historyImageView)
            binding.createdTextView.text = history.timestamp

            binding.root.setOnClickListener {
                onItemClick(history)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val history = getItem(position)
        holder.bind(history)
    }

    class HistoryDiffCallBack : DiffUtil.ItemCallback<HistoryResponse>() {
        override fun areItemsTheSame(oldItem: HistoryResponse, newItem: HistoryResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryResponse, newItem: HistoryResponse): Boolean {
            return oldItem == newItem
        }
    }
}
