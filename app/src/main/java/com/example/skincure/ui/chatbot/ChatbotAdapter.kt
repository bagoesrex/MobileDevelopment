package com.example.skincure.ui.chatbot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.skincure.R
import com.example.skincure.data.remote.response.ChatMessage

class ChatBotAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback()) {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_USER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chatbot_request_item, parent, false)
            UserMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.chatbot_reponse_item, parent, false)
            BotResponseViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is UserMessageViewHolder) {
            holder.bind(message)
        } else if (holder is BotResponseViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    inner class UserMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text_view)

        fun bind(message: ChatMessage) {
            messageTextView.text = message.message
        }
    }

    inner class BotResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.message_text_view)

        fun bind(message: ChatMessage) {
            messageTextView.text = message.message
        }
    }

    class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.message == newItem.message
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}