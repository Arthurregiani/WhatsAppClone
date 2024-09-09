package br.edu.ifsp.dmo.whatsapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    // LiveData que indica o estado de logout
    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> get() = _logoutStatus

    init {
        // Observa mudanças no estado de autenticação do usuário
        userRepository.authStatus.observeForever { isAuthenticated ->
            if (!isAuthenticated) {
                _logoutStatus.value = true // Marca o logout como detectado
            }
        }
    }

    // Função para realizar o logout
    fun logout() {
        userRepository.logout() // Chama o método de logout no UserRepository
    }
}
