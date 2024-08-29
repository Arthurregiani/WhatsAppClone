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
        usuarioRepository.cadastrarAutenticacaoUsuario(email, password) { success, uid, messageError ->
            // se o cadastro for bem-sucedido passa o uid da autenticação
            // para o banco de dados
            if (success && uid != null) {
                val user = Usuario(userName, email)
                usuarioRepository.cadastrarUsuarioDatabase(uid, user)
                _cadastroStatus.value = Pair(true, null)
            } else {
                _cadastroStatus.value = Pair(false, messageError)
            }
        }
    }
}
