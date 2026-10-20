package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ChatViewModel(
    private val chatRepository: ChatRepository,
    val userRepository: UserRepository,
    contactName: String,
    contactProfileImageUrl: String?
) : ViewModel() {
    val messages = MutableLiveData<List<Message>>(emptyList())
    val chatId = MutableLiveData<String?>()
    val contactName = MutableLiveData(contactName)
    val contactProfileImageUrl = MutableLiveData(contactProfileImageUrl)
    val error = MutableLiveData<String?>()
    val sending = MutableLiveData(false)
    val sent = MutableLiveData<String?>()
    val hasMore = MutableLiveData(false)
    private val mutex = Mutex()
    private var retry: Pair<String, String>? = null

    suspend fun refresh(email: String) = mutex.withLock {
        try {
            val id = chatId.value ?: chatRepository.getOrCreateChat(email).also { chatId.value = it }
            val old = messages.value.orEmpty()
            if (old.isEmpty()) {
                val (latest, more) = chatRepository.messages(id)
                messages.value = latest
                hasMore.value = more
            } else {
                var cursor = old.last().id
                do {
                    val (page, more) = chatRepository.messages(id, after = cursor)
                    messages.value = (messages.value.orEmpty() + page).distinctBy { it.id }.sortedBy { it.id.toLong() }
                    if (page.isNotEmpty()) cursor = page.last().id
                } while (more && page.isNotEmpty())
            }
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { error.value = e.message }
    }
    fun older() {
        viewModelScope.launch {
            mutex.withLock {
                try {
                    val id = chatId.value ?: return@withLock
                    val before = messages.value?.firstOrNull()?.id ?: return@withLock
                    val (page, more) = chatRepository.messages(id, before)
                    messages.value = (page + messages.value.orEmpty()).distinctBy { it.id }.sortedBy { it.id.toLong() }
                    hasMore.value = more
                } catch (e: CancellationException) { throw e }
                catch (e: Exception) { error.value = e.message }
            }
        }
    }
    fun sendMessage(text: String) {
        if (sending.value == true) return
        val id = chatId.value ?: run { error.value = "Aguarde a conexão com a conversa."; return }
        val clientId = retry?.takeIf { it.first == text }?.second ?: UUID.randomUUID().toString()
        retry = text to clientId
        sending.value = true
        viewModelScope.launch {
            try {
                val message = chatRepository.sendMessage(id, text, clientId)
                mutex.withLock { messages.value = (messages.value.orEmpty() + message).distinctBy { it.id }.sortedBy { it.id.toLong() } }
                retry = null
                sent.value = text
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = e.message }
            finally { sending.value = false }
        }
    }
}
