package br.edu.ifsp.dmo.whatsapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository


class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        userRepository.authStatus.observeForever { isAuthenticated ->
            _authStatus.value = isAuthenticated
        }
    }

    fun autenticarUsuario(email: String, senha: String) {
        userRepository.validarAutenticacao(email, senha) { sucesso, mensagemErro ->
            if (sucesso) {
                _authStatus.value = true // Login bem-sucedido
            } else {
                _authStatus.value = false // Falha no login
            }
        }
    }
}
