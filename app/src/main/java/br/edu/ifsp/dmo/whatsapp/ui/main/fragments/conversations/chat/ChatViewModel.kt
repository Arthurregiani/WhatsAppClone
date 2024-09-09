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
    val userRepository: UserRepository,
    contactName: String,
    contactProfileImageUrl: String?
) : ViewModel() {

    // LiveData para observar a lista de mensagens
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    // LiveData para observar o ID do chat
    private val _chatId = MutableLiveData<String?>()
    val chatId: LiveData<String?> get() = _chatId

    // LiveData para observar o nome e a URL da imagem de perfil do contato
    val contactName = MutableLiveData<String>().apply { value = contactName }
    val contactProfileImageUrl = MutableLiveData<String?>().apply { value = contactProfileImageUrl }

    // Verifica se o chat existe e cria um novo se necessário
    fun checkOrCreateChat(contactEmail: String) {
        viewModelScope.launch {
            val currentUserId = userRepository.getCurrentUserUid() ?: return@launch
            val contactUserId = userRepository.getUidByEmail(contactEmail) ?: return@launch

            val participants = listOf(currentUserId, contactUserId)
            val chatId = chatRepository.getOrCreateChat(participants)

            _chatId.postValue(chatId)
            chatId?.let { observeMessages(it) }
        }
    }

    // Envia uma mensagem para o chat
    fun sendMessage(chatId: String, messageText: String, senderId: String) {
        val message = Message(senderId, messageText, System.currentTimeMillis())
        viewModelScope.launch {
            chatRepository.sendMessage(chatId, message)
        }
    }

    // Observa as mensagens do chat em tempo real
    fun observeMessages(chatId: String) {
        chatRepository.getMessagesCollection(chatId)
            .orderBy("timestamp")
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

    // Método para lidar com falhas
    private fun handleFailure(tag: String, e: Exception) {
        println("Erro no $tag: ${e.message}")
    }
}
