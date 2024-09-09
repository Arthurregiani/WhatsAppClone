package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.model.User
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    // LiveData para a URL da imagem do perfil
    private val _profileImageUri = MutableLiveData<String?>()
    val profileImageUri: LiveData<String?> get() = _profileImageUri

    // LiveData para o nome do perfil
    private val _profileName = MutableLiveData<String?>()
    val profileName: LiveData<String?> get() = _profileName

    // LiveData para o status de upload da imagem
    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    // LiveData para o status de sucesso do upload do nome
    private val _uploadNameSuccess = MutableLiveData<Boolean>()
    val uploadNameSuccess: LiveData<Boolean> get() = _uploadNameSuccess

    init {
        // Carrega os dados do perfil quando o ViewModel é inicializado
        loadProfileData()
    }

    // Carrega os dados do perfil do repositório
    private fun loadProfileData() {
        viewModelScope.launch {
            _profileImageUri.postValue(userRepository.getProfileImageUrl())
            userRepository.getDataCurrentUser { userData ->
                _profileName.postValue(userData?.nome)
            }
        }
    }

    // Função para atualizar o nome do perfil
    fun uploadProfileName(name: String) {
        viewModelScope.launch {
            try {
                userRepository.uploadProfileName(name)
                _profileName.postValue(name)
                _uploadNameSuccess.postValue(true)
            } catch (e: Exception) {
                _uploadNameSuccess.postValue(false)
            }
        }
    }

    // Função para atualizar a imagem do perfil
    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _profileImageUri.postValue(userRepository.uploadProfileImage(imageUri))
                _uploadStatus.postValue(true)
            } catch (e: Exception) {
                _uploadStatus.postValue(false)
            }
        }
    }
}
