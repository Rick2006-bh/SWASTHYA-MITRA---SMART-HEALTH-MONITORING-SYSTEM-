package com.example.swasthyamitra

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<MessageModel>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private companion object {
        const val VIEW_TYPE_USER = 1
        const val VIEW_TYPE_AI = 2
    }

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userText: TextView? = view.findViewById(R.id.userMessageText)
        val aiText: TextView? = view.findViewById(R.id.aiMessageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_USER) R.layout.item_user else R.layout.item_ai
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        if (message.sender == "user") {
            holder.userText?.text = message.message
        } else {
            holder.aiText?.text = message.message
        }
    }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == "user") VIEW_TYPE_USER else VIEW_TYPE_AI
    }
}
