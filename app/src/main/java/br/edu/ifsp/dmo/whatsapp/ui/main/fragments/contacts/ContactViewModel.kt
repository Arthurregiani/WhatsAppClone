package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.launch

class ContactViewModel(private val userRepository: UserRepository) : ViewModel() {

    // Adiciona um contato usando o UserRepository
    fun addContact(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val success = userRepository.addContact(email)
                // Notifica o resultado da operação
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

    // Obtém a lista de contatos do UserRepository
    fun getContacts(onResult: (List<Contact>) -> Unit) {
        viewModelScope.launch {
            try {
                val contacts = userRepository.getContacts()
                // Notifica a lista de contatos
                onResult(contacts)
            } catch (e: Exception) {
                onResult(emptyList()) // Retorna uma lista vazia em caso de erro
            }
        }
    }
}
