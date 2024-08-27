package br.edu.ifsp.dmo.whatsapp.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.repositories.UsuarioRepository

class LoginViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _authStatus = MutableLiveData<Result<Boolean>>()
    val authStatus: LiveData<Result<Boolean>> get() = _authStatus

    fun autenticarUsuario(email: String, senha: String) {
        usuarioRepository.validarAutenticacao(email, senha) { sucesso, mensagemErro ->
            if (sucesso) {
                _authStatus.value = Result.success(true)
            } else {
                _authStatus.value = Result.failure(Exception(mensagemErro ?: "Erro desconhecido"))
            }
        }
    }
}
