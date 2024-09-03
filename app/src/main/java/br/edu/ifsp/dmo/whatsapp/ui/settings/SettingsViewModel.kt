package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.ImageRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileImageUri = MutableLiveData<String?>()
    val profileImageUri: LiveData<String?> get() = _profileImageUri

    private val _profileName = MutableLiveData<String?>()
    val profileName: LiveData<String?> get() = _profileName

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private val _uploadNameSuccess = MutableLiveData<Boolean>()
    val uploadNameSuccess: LiveData<Boolean> get() = _uploadNameSuccess

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _profileImageUri.postValue(imageRepository.getProfileImageUrl())
            userRepository.getDataCurrentUser { userData ->
                _profileName.postValue(userData?.nome)
            }
        }
    }

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

    fun uploadProfileImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _profileImageUri.postValue(imageRepository.uploadProfileImage(imageUri))
                _uploadStatus.postValue(true)
            } catch (e: Exception) {
                _uploadStatus.postValue(false)
            }
        }
    }
}
