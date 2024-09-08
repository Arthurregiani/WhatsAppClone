package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatViewModelFactory(
    private val contactName: String,
    private val contactProfileImageUrl: String?
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(contactName, contactProfileImageUrl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
