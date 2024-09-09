package br.edu.ifsp.dmo.whatsapp.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import br.edu.ifsp.dmo.whatsapp.data.model.User
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {

    // LiveData para observar o status do cadastro
    private val _cadastroStatus = MutableLiveData<Pair<Boolean, String?>>()
    val cadastroStatus: LiveData<Pair<Boolean, String?>> get() = _cadastroStatus

    // Função para cadastrar um novo usuário
    fun cadastrarUsuario(userName: String, email: String, password: String) {
        userRepository.cadastrarAutenticacaoUsuario(email, password)
        { success, uid, messageError ->
            if (success && uid != null) {
                // Se o cadastro for bem-sucedido,
                // cria um objeto User e o salva no banco de dados
                val user = User(userName, email)
                userRepository.cadastrarUsuarioDatabase(uid, user)
                _cadastroStatus.value = Pair(true, null) // Sucesso no cadastro

            } else {
                // Se falhar, define o status de erro
                _cadastroStatus.value = Pair(false, messageError)
            }
        }
    }
}
