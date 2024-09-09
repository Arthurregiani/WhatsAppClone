package br.edu.ifsp.dmo.whatsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    // Cria uma instância de ViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se o ViewModel solicitado é do tipo MainViewModel
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Retorna uma nova instância de MainViewModel
            return MainViewModel(userRepository) as T
        }
        // Lança uma exceção se o ViewModel solicitado for desconhecido
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
