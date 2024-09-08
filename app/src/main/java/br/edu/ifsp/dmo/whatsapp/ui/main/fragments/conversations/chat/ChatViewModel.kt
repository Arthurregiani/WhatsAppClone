package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.launch


class ChatViewModel(
    private val chatRepository: ChatRepository,
    val userRepository: UserRepository
) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _chatId = MutableLiveData<String?>()
    val chatId: LiveData<String?> get() = _chatId

    val contactName = MutableLiveData<String>()
    val contactProfileImageUrl = MutableLiveData<String>()

    fun createChat(participants: List<String>, callback: (String?) -> Unit) {
        viewModelScope.launch {
            val chatId = chatRepository.createChat(participants)
            _chatId.postValue(chatId)
            callback(chatId)
            if (chatId != null) {
                observeMessages(chatId)
            }
        }
    }

    fun sendMessage(chatId: String, messageText: String, senderId: String) {
        val message = Message(senderId, messageText, System.currentTimeMillis())
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, message)
        }
    }

    fun observeMessages(chatId: String) {
        chatRepository.getMessagesCollection(chatId)
            .orderBy("timestamp") // Ordena as mensagens pelo timestamp em ordem crescente
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    handleFailure("ChatViewModel", e)
                    return@addSnapshotListener
                }
                val messagesList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()
                _messages.postValue(messagesList)
            }
    }


    private fun handleFailure(tag: String, e: Exception) {
        // Handle the error appropriately
        println("Erro no $tag: ${e.message}")
    }
}
