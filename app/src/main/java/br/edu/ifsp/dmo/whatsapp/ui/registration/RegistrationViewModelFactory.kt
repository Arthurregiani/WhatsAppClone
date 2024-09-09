package br.edu.ifsp.dmo.whatsapp.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class RegistrationViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se a classe ViewModel é do tipo RegistrationViewModel
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            // Cria uma nova instância do RegistrationViewModel
            return RegistrationViewModel(userRepository) as T
        }
        // Lança uma exceção se a classe ViewModel não for reconhecida
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
