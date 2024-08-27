package br.edu.ifsp.dmo.whatsapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.repositories.UsuarioRepository

class MainViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> get() = _logoutStatus

    init {
        // Observa mudanças no estado de autenticação
        usuarioRepository.authStatus.observeForever { isAuthenticated ->
            if (!isAuthenticated) {
                _logoutStatus.value = true // Logout detectado
            }
        }
    }


    fun logout() {
        usuarioRepository.logout()
    }
}
