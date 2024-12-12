package com.example.skincure.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.data.remote.response.HistoriesItem
import com.example.skincure.databinding.HistoryItemBinding
import com.example.skincure.utils.dateFormatter
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val onItemClick: (HistoriesItem) -> Unit,
) : PagingDataAdapter<HistoriesItem, HistoryAdapter.HistoryViewHolder>(DIFF_CALLBACK) {

    class HistoryViewHolder(
        private val binding: HistoryItemBinding,
        private val onItemClick: (HistoriesItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(histories: HistoriesItem) {
            binding.tittleTextView.text = histories.result
            binding.descriptionTextView.text = histories.description

            Picasso
                .get()
                .load(histories.imageUrl)
                .resize(400, 400)
                .into(binding.historyImageView)

            val formattedDate = dateFormatter(histories.createdAt)
            binding.createdTextView.text = formattedDate

            binding.scoreTextView.text = buildString {
                append("Prediction Score: ")
                val score = histories.confidenceScore.toDoubleOrNull() ?: 0.0
                append(score.toInt())
                append("%")
            }

            binding.root.setOnClickListener {
                onItemClick(histories)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val histories = getItem(position)
        if (histories != null) {
            holder.bind(histories)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<HistoriesItem>() {
            override fun areItemsTheSame(
                oldItem: HistoriesItem,
                newItem: HistoriesItem
            ): Boolean {
                return oldItem.uid == newItem.uid
            }

            override fun areContentsTheSame(
                oldItem: HistoriesItem,
                newItem: HistoriesItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
