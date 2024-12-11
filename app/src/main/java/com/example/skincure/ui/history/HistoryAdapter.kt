package com.example.skincure.ui.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.databinding.HistoryItemBinding
import com.example.skincure.utils.dateFormatter
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val onItemClick: (Map<String, Any>) -> Unit,
) : ListAdapter<Map<String, Any>, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    class HistoryViewHolder(
        private val binding: HistoryItemBinding,
        private val onItemClick: (Map<String, Any>) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Map<String, Any>) {
            binding.tittleTextView.text = data["diseaseName"] as? String ?: "Unknown Disease"
            binding.descriptionTextView.text = data["description"] as? String ?: "No description"
            val imageUrl = data["imageUri"] as? String
            val timestamp = data["timestamp"] as? String
            val formattedDate = timestamp?.let { dateFormatter(it) }
            Picasso
                .get()
                .load(imageUrl)
                .resize(400, 400)
                .into(binding.historyImageView)
            binding.createdTextView.text = formattedDate

            binding.scoreTextView.text = buildString {
                append("Prediction Score: ")
                val score = (data["score"] as? String)?.toDoubleOrNull() ?: 0.0
                append(score.toInt())
                append("%")
            }

            binding.root.setOnClickListener {
                onItemClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(data)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Map<String, Any>>() {
            override fun areItemsTheSame(
                oldItem: Map<String, Any>,
                newItem: Map<String, Any>
            ): Boolean {
                return oldItem["id"] == newItem["id"]
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Map<String, Any>,
                newItem: Map<String, Any>
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}