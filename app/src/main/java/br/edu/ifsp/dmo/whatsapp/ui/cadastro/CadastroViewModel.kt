package br.edu.ifsp.dmo.whatsapp.ui.cadastro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import br.edu.ifsp.dmo.whatsapp.data.repositories.UsuarioRepository

class CadastroViewModel(private val usuarioRepository: UsuarioRepository) : ViewModel() {

    private val _cadastroStatus = MutableLiveData<Pair<Boolean, String?>>()
    val cadastroStatus: LiveData<Pair<Boolean, String?>> get() = _cadastroStatus

    fun cadastrarUsuario(userName: String, email: String, password: String) {
        val user = Usuario(userName, email, password)

        usuarioRepository.cadastrarUsuario(user) { success, messageError ->
            _cadastroStatus.value = Pair(success, messageError)
        }
    }
}
