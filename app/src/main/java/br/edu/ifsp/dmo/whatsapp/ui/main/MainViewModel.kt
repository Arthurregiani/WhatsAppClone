package br.edu.ifsp.dmo.whatsapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {
    val query = androidx.lifecycle.MutableLiveData("")
    val logoutStatus = userRepository.authStatus.map { !it }
    fun logout() {
        viewModelScope.launch {
            try { userRepository.logout() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { /* The local session has already been cleared. */ }
        }
    }
}
