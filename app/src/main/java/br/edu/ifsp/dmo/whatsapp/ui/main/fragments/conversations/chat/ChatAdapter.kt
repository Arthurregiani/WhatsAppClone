package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import br.edu.ifsp.dmo.whatsapp.databinding.ItemMessageSentBinding
import br.edu.ifsp.dmo.whatsapp.databinding.ItemMessageReceivedBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class ChatAdapter(private var messages: List<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    fun updateMessages(newMessages: List<Message>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserUid) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else {
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.textViewMessage.text = message.messageText
            binding.textViewTimestamp.text = formatTimestamp(message.timestamp)
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.textViewMessage.text = message.messageText
            binding.textViewTimestamp.text = formatTimestamp(message.timestamp)
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()) // Formato de hora de 24 horas
        return sdf.format(timestamp)
    }
}
