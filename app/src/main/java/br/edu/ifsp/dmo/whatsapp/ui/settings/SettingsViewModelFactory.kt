package br.edu.ifsp.dmo.whatsapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class SettingsViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica se o ViewModel solicitado é do tipo SettingsViewModel
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            // Retorna uma nova instância do SettingsViewModel
            return SettingsViewModel(userRepository) as T
        }
        // Lança uma exceção se o ViewModel solicitado for desconhecido
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
