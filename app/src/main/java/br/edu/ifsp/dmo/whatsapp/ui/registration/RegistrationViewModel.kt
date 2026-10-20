package br.edu.ifsp.dmo.whatsapp.ui.registration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegistrationViewModel(private val userRepository: UserRepository) : ViewModel() {
    val cadastroStatus = MutableLiveData<Pair<Boolean, String?>>()
    val busy = MutableLiveData(false)
    fun cadastrarUsuario(userName: String, email: String, password: String) {
        if (busy.value == true) return
        busy.value = true
        viewModelScope.launch {
            try {
                userRepository.register(userName, email, password)
                cadastroStatus.value = true to null
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { cadastroStatus.value = false to e.message }
            finally { busy.value = false }
        }
    }
}
