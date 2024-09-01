package br.edu.ifsp.dmo.whatsapp.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.ifsp.dmo.whatsapp.data.repositories.ImageRepository
import kotlinx.coroutines.launch
import android.net.Uri

class SettingsViewModel(private val imageRepository: ImageRepository) : ViewModel() {

    private val _profileImageUri = MutableLiveData<String?>()
    val profileImageUri: MutableLiveData<String?> get() = _profileImageUri

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    init {
        loadProfileImage()
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
}
