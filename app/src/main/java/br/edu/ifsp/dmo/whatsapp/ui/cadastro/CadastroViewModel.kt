package br.edu.ifsp.dmo.whatsapp.ui.cadastro

import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import br.edu.ifsp.dmo.whatsapp.data.repository.UsuarioRepository

class CadastroViewModel: ViewModel() {

    private val repository = UsuarioRepository()

    fun cadastrarUsuario(nome: String, email: String, senha: String) {
        val usuario = Usuario(nome, email, senha)
        return repository.cadastrarUsuario(usuario)
    }






}