package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.CancellationException

class ContactViewModel(private val userRepository: UserRepository) : ViewModel() {
    val contacts = MutableLiveData<List<Contact>>(emptyList())
    val error = MutableLiveData<String?>()
    suspend fun refresh() {
        try { contacts.value = userRepository.getContacts(); error.value = null }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { error.value = e.message }
    }
    suspend fun add(email: String) { userRepository.addContact(email); refresh() }
}
