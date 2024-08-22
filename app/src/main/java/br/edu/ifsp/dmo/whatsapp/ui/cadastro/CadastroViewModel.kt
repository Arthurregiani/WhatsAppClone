package br.edu.ifsp.dmo.whatsapp.ui.cadastro

import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import br.edu.ifsp.dmo.whatsapp.data.repository.UserRepository

class CadastroViewModel: ViewModel() {

    private val repository = UserRepository()

    fun cadastrarUsuario(nome: String, email: String, senha: String) {
        val usuario = Usuario(nome, email, senha)
        repository.cadastrarUsuario(usuario)
    }






}