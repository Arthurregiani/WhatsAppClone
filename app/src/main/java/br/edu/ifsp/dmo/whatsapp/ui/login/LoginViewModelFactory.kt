package br.edu.ifsp.dmo.whatsapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class LoginViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se o ViewModel é do tipo LoginViewModel
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            // Retorna uma instância de LoginViewModel com o UserRepository fornecido
            return LoginViewModel(userRepository) as T
        }
        // Lança uma exceção se o ViewModel não for reconhecido
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
