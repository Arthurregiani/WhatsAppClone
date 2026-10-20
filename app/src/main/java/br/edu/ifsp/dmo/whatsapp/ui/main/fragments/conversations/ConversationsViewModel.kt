package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.Chat
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import kotlinx.coroutines.CancellationException

class ConversationsViewModel : ViewModel() {
    val chats = MutableLiveData<List<Chat>>(emptyList())
    val error = MutableLiveData<String?>()
    suspend fun refresh() {
        try { chats.value = ChatRepository().chats(); error.value = null }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { error.value = e.message }
    }
}
