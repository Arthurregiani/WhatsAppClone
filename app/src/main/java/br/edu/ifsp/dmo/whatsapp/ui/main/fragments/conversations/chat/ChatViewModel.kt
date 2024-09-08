package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(contactName: String, contactProfileImageUrl: String?) : ViewModel() {

    private val chatRepository = ChatRepository()
    private val _contactName = MutableLiveData<String>()
    val contactName: LiveData<String> = _contactName

    private val _contactProfileImageUrl = MutableLiveData<String>()
    val contactProfileImageUrl: LiveData<String> = _contactProfileImageUrl

    private val _chatId = MutableLiveData<String?>()
    val chatId: LiveData<String?> = _chatId

    init {
        _contactName.value = contactName
        _contactProfileImageUrl.value = contactProfileImageUrl ?: ""
    }

    fun createChat(participants: List<String>, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val id = chatRepository.createChat(participants)
            _chatId.value = id
            callback(id)
        }
    }

    fun sendMessage(chatId: String, messageText: String, senderId: String) {
        val message = Message(
            senderId = senderId,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, message)
        }
    }
}
