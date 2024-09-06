package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.launch

class ContactViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Função para adicionar o contato
    fun addContact(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = userRepository.addContact(email)
                if (success) {
                    onResult(true, null)
                } else {
                    onResult(false, "Falha ao adicionar contato")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // Função para obter a lista de contatos
    fun getContacts(onResult: (List<Contact>) -> Unit) {
        viewModelScope.launch {
            try {
                val contacts = userRepository.getContacts()
                onResult(contacts)
            } catch (e: Exception) {
                onResult(emptyList())
            }
        }
    }
}
