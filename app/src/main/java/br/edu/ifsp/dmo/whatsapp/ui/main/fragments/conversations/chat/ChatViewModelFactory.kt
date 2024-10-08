package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class ChatViewModelFactory(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val contactName: String,
    private val contactProfileImageUrl: String?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatRepository, userRepository, contactName, contactProfileImageUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
