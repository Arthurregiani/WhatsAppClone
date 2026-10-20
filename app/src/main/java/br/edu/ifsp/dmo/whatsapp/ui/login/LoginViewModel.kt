package br.edu.ifsp.dmo.whatsapp.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {
    val authStatus = userRepository.authStatus
    val error = MutableLiveData<String?>()
    val busy = MutableLiveData(false)
    fun autenticarUsuario(email: String, senha: String) {
        if (busy.value == true) return
        busy.value = true
        viewModelScope.launch {
            try { userRepository.login(email, senha) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = e.message }
            finally { busy.value = false }
        }
    }
}
