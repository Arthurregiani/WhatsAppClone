package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class ContactViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se o ViewModel é do tipo ContactViewModel
        if (modelClass.isAssignableFrom(ContactViewModel::class.java)) {
            // Retorna uma instância de ContactViewModel com o UserRepository fornecido
            return ContactViewModel(userRepository) as T
        }
        // Lança uma exceção se o ViewModel não for reconhecido
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
