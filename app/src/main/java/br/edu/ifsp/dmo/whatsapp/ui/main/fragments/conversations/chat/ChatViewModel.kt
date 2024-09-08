package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChatViewModel(contactName: String, contactProfileImageUrl: String?) : ViewModel() {

    private val _contactName = MutableLiveData<String>()
    val contactName: LiveData<String> = _contactName

    private val _contactProfileImageUrl = MutableLiveData<String>()
    val contactProfileImageUrl: LiveData<String> = _contactProfileImageUrl

    init {
        // Inicializa os dados no ViewModel
        _contactName.value = contactName
        _contactProfileImageUrl.value = contactProfileImageUrl ?: ""
    }
}
