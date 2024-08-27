package br.edu.ifsp.dmo.whatsapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.repositories.UsuarioRepository


class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        usuarioRepository.authStatus.observeForever { isAuthenticated ->
            _authStatus.value = isAuthenticated
        }
    }

    fun autenticarUsuario(email: String, senha: String) {
        usuarioRepository.validarAutenticacao(email, senha) { sucesso, mensagemErro ->
            if (sucesso) {
                _authStatus.value = true // Login bem-sucedido
            } else {
                _authStatus.value = false // Falha no login
            }
        }
    }
}
