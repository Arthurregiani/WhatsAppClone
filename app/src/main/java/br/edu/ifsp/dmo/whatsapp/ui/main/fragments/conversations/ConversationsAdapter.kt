package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.model.Chat
import br.edu.ifsp.dmo.whatsapp.databinding.ItemContactBinding
import com.bumptech.glide.Glide
import java.text.DateFormat
import java.util.Date

class ConversationsAdapter(private val onClick: (Chat) -> Unit) : RecyclerView.Adapter<ConversationsAdapter.Holder>() {
    private var rows = emptyList<Chat>()
    fun submitList(chats: List<Chat>) { rows = chats; notifyDataSetChanged() }
    override fun getItemCount() = rows.size
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: Holder, position: Int) { holder.bind(rows[position]) }
    inner class Holder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: Chat) {
            binding.textContactName.text = chat.contact.nome
            val time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(chat.lastMessageTimestamp))
            binding.textContactEmail.text = "${chat.lastMessage.ifEmpty { "Conversa iniciada" }} · $time"
            Glide.with(binding.root).load(chat.contact.profileImageUrl ?: R.drawable.user_image_default).into(binding.contactProfileImage)
            binding.root.setOnClickListener { onClick(chat) }
        }
    }
}
