package com.odontosystem.app.ui.chatbot

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.odontosystem.app.R
import com.odontosystem.app.data.model.ChatMessage
import com.odontosystem.app.data.model.SenderType
import com.odontosystem.app.databinding.ItemChatMessageBinding

class ChatbotAdapter : RecyclerView.Adapter<ChatbotAdapter.ChatViewHolder>() {

    private var messages: List<ChatMessage> = emptyList()

    fun submitList(newList: List<ChatMessage>) {
        messages = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatMessage: ChatMessage) {
            val context = binding.root.context
            binding.tvChatMessage.text = chatMessage.text

            val params = binding.tvChatMessage.layoutParams as LinearLayout.LayoutParams

            if (chatMessage.sender == SenderType.USER) {
                params.gravity = Gravity.END
                binding.tvChatMessage.background = ContextCompat.getDrawable(context, R.drawable.bg_chat_user)
                binding.tvChatMessage.setTextColor(ContextCompat.getColor(context, R.color.chat_user_text))
            } else {
                params.gravity = Gravity.START
                binding.tvChatMessage.background = ContextCompat.getDrawable(context, R.drawable.bg_chat_bot)
                binding.tvChatMessage.setTextColor(ContextCompat.getColor(context, R.color.chat_bot_text))
            }

            binding.tvChatMessage.layoutParams = params
        }
    }
}
