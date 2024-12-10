package com.example.skincure.ui.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.data.local.FavoriteResult
import com.example.skincure.databinding.FavoriteItemBinding
import com.example.skincure.utils.DateUtils
import com.squareup.picasso.Picasso


class FavoriteAdapter(
    private val onItemClick: (FavoriteResult) -> Unit
) : ListAdapter<FavoriteResult, FavoriteAdapter.FavoriteViewHolder>(FavoriteDiffCallBack()) {

    class FavoriteViewHolder(
        private val binding: FavoriteItemBinding,
        private val onItemClick: (FavoriteResult) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(fav: FavoriteResult) {
            binding.tittleTextView.text = fav.diseaseName
            binding.descriptionTextView.text = fav.description
            Picasso.get()
                .load(fav.imageUri)
                .into(binding.favsImageView)
            val timestamp = fav.timestamp
            val formattedDate = DateUtils.formatTimestamp((timestamp as Long))
            binding.createdTextView.text = formattedDate
            binding.root.setOnClickListener {
                onItemClick(fav)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = FavoriteItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val fav = getItem(position)
        holder.bind(fav)
    }

    class FavoriteDiffCallBack : DiffUtil.ItemCallback<FavoriteResult>() {
        override fun areItemsTheSame(oldItem: FavoriteResult, newItem: FavoriteResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavoriteResult, newItem: FavoriteResult): Boolean {
            return oldItem == newItem
        }
    }
}
