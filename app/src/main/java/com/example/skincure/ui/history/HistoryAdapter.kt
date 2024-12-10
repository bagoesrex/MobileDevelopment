package com.example.skincure.ui.history

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.databinding.HistoryItemBinding
import com.example.skincure.utils.dateFormatter
import com.squareup.picasso.Picasso

class HistoryAdapter(
    private val onItemClick: (Map<String, Any>) -> Unit,
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val dataList = mutableListOf<Map<String, Any>>()

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
            Picasso.get().load(imageUrl).into(binding.historyImageView)
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

    fun submitList(newList: List<Map<String, Any>>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val data = dataList[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int = dataList.size
}
