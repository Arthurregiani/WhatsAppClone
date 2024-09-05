package br.edu.ifsp.dmo.whatsapp.data.repositories

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class ImageRepository(private val firebaseAuth: FirebaseAuth) {

    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Upload da imagem de perfil no Firebase Storage e salva o URL no Firestore
    suspend fun uploadProfileImage(imageUri: Uri): String? {
        val uid = firebaseAuth.currentUser?.uid ?: return null
        val profileImageRef = storageRef.child("profile_images/$uid.jpg")

        // Upload da imagem
        val downloadUrl = profileImageRef.putFile(imageUri).await().storage.downloadUrl.await().toString()

        // Salvar o URL no Firestore sob o documento do usuário
        usersCollection.document(uid).update("profileImageUrl", downloadUrl).await()

        return downloadUrl
    }

    // Recupera o URL da imagem de perfil a partir do Firestore
    suspend fun getProfileImageUrl(): String? {
        val uid = firebaseAuth.currentUser?.uid ?: return null

        // Tenta obter o documento do usuário no Firestore
        val userDoc = usersCollection.document(uid).get().await()

        // Verifica se o campo profileImageUrl existe no Firestore
        return if (userDoc.exists()) {
            userDoc.getString("profileImageUrl")
        } else {
            null
        }
    }
}
