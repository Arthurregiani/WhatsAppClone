package br.edu.ifsp.dmo.whatsapp.data.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ImageRepository(private val firebaseAuth: FirebaseAuth) {

    private val storageRef = FirebaseStorage.getInstance().reference

    suspend fun uploadProfileImage(imageUri: Uri): String? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        val profileImageRef = storageRef.child("profile_images/$uid.jpg")
        return profileImageRef.putFile(imageUri).await().storage.downloadUrl.await().toString()
    }

    suspend fun getProfileImageUrl(): String? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        return storageRef.child("profile_images/$uid.jpg").downloadUrl.await().toString()
    }
}
