package br.edu.ifsp.dmo.whatsapp.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.User
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _cadastroStatus = MutableLiveData<Pair<Boolean, String?>>()
    val cadastroStatus: LiveData<Pair<Boolean, String?>> get() = _cadastroStatus

    fun cadastrarUsuario(userName: String, email: String, password: String) {
        userRepository.cadastrarAutenticacaoUsuario(email, password) { success, uid, messageError ->
            // se o cadastro for bem-sucedido passa o uid da autenticação
            // para o banco de dados
            if (success && uid != null) {
                val user = User(userName, email)
                userRepository.cadastrarUsuarioDatabase(uid, user)
                _cadastroStatus.value = Pair(true, null)
            } else {
                _cadastroStatus.value = Pair(false, messageError)
            }
        }
    }
}
