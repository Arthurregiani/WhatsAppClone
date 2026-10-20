package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class SettingsViewModel(private val userRepository: UserRepository) : ViewModel() {
    val profileImageUri = MutableLiveData<String?>()
    val profileName = MutableLiveData<String?>()
    val uploadStatus = MutableLiveData<Boolean>()
    val uploadNameSuccess = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()
    init {
        viewModelScope.launch {
            try {
                val user = userRepository.currentUser()
                profileImageUri.value = user.profileImageUrl
                profileName.value = user.nome
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = e.message }
        }
    }
    fun uploadProfileName(name: String) {
        viewModelScope.launch {
            try {
                userRepository.uploadProfileName(name.trim())
                profileName.value = name.trim()
                uploadNameSuccess.value = true
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = e.message; uploadNameSuccess.value = false }
        }
    }
    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                profileImageUri.value = userRepository.uploadProfileImage(uri)
                uploadStatus.value = true
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error.value = e.message; uploadStatus.value = false }
        }
    }
}
