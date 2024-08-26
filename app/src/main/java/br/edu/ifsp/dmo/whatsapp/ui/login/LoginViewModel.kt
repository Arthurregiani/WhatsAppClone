package br.edu.ifsp.dmo.whatsapp.ui.login

import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.repository.UsuarioRepository

class LoginViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    fun logarUsuario(email: String, senha: String){
        repository.validarAutenticacao(email, senha)
    }
}