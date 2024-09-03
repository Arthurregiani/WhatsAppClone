package br.edu.ifsp.dmo.whatsapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.ImageRepository
import kotlinx.coroutines.launch
import android.net.Uri
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class SettingsViewModel(private val imageRepository: ImageRepository, private val userRepository: UserRepository) : ViewModel() {

    private val _profileImageUri = MutableLiveData<String?>()
    val profileImageUri: MutableLiveData<String?> get() = _profileImageUri

    private val _profileName = MutableLiveData<String?>()
    val profileName: MutableLiveData<String?> get() = _profileName

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private val _uploadNameSuccess = MutableLiveData<Boolean>()
    val uploadNameSuccess: LiveData<Boolean> get() = _uploadNameSuccess

    init {
        loadProfileImage()
        loadProfileName()
    }

    private fun loadProfileImage() {
        viewModelScope.launch {
            try {
                val imageUrl = imageRepository.getProfileImageUrl()
                _profileImageUri.postValue(imageUrl)
            } catch (e: Exception) {
                _profileImageUri.postValue(null)
            }
        }
    }

    private fun loadProfileName() {
        viewModelScope.launch {
            try {
                userRepository.getDataCurrentUser { userData ->
                    _profileName.postValue(userData?.nome)
                }
            } catch (e: Exception) {
                _profileName.postValue(null)
            }
        }
    }


    fun uploadProfileName(name: String) {
        viewModelScope.launch {
            try {
                userRepository.uploadProfileName(name)
                _profileName.postValue(name)
                // Notifica que o upload foi bem-sucedido
                _uploadNameSuccess.postValue(true)
            } catch (e: Exception) {
                _uploadNameSuccess.postValue(false)
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                val imageUrl = imageRepository.uploadProfileImage(imageUri)
                _profileImageUri.postValue(imageUrl)
                _uploadStatus.postValue(true)
            } catch (e: Exception) {
                _uploadStatus.postValue(false)
            }
        }
    }

}
