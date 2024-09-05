package br.edu.ifsp.dmo.whatsapp.data.repositories

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.dmo.whatsapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val usersCollection = firestore.collection("users")

    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authStatus.value = firebaseAuth.currentUser != null
        }
    }

    // Método auxiliar para obter o UID do usuário atual
    private fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    // Método auxiliar para lidar com falhas de operação
    private fun handleFailure(tag: String, e: Exception) {
        Log.e(tag, "Erro: ${e.message}")
    }

    // Método para cadastrar autenticação do usuário
    fun cadastrarAutenticacaoUsuario(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, getCurrentUserUid(), null)
                } else {
                    callback(false, null, task.exception?.message)
                }
            }
    }

    // Método para cadastrar usuário no Firestore
    fun cadastrarUsuarioDatabase(uid: String, user: User) {
        usersCollection.document(uid).set(user)
            .addOnSuccessListener {
                Log.d("UserRepository", "Usuário cadastrado com sucesso no Firestore.")
            }
            .addOnFailureListener { e ->
                handleFailure("UserRepository", e)
            }
    }

    // Método para validar autenticação
    fun validarAutenticacao(email: String, senha: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    // Método para realizar logout
    fun logout() {
        auth.signOut()
    }

    // Método para obter dados do usuário autenticado
    fun getDataCurrentUser(callback: (User?) -> Unit) {
        getCurrentUserUid()?.let { uid ->
            usersCollection.document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    val user = documentSnapshot.toObject(User::class.java)
                    callback(user)
                }
                .addOnFailureListener { exception ->
                    handleFailure("UserRepository", exception)
                    callback(null)
                }
        } ?: callback(null)
    }

    // Método para atualizar o nome do perfil
    fun uploadProfileName(name: String) {
        getCurrentUserUid()?.let { uid ->
            usersCollection.document(uid).update("nome", name)
                .addOnSuccessListener {
                    Log.d("UserRepository", "Nome atualizado com sucesso no Firestore.")
                }
                .addOnFailureListener { e ->
                    handleFailure("UserRepository", e)
                }
        }
    }

    // -------------------
    // Integração com Firebase Storage e Firestore para imagem de perfil
    // -------------------

    // Upload da imagem de perfil no Firebase Storage e salva o URL no Firestore
    suspend fun uploadProfileImage(imageUri: Uri): String? {
        val uid = getCurrentUserUid() ?: return null
        val profileImageRef = storageRef.child("profile_images/$uid.jpg")

        return try {
            // Upload da imagem
            val downloadUrl = profileImageRef.putFile(imageUri).await().storage.downloadUrl.await().toString()

            // Salvar o URL no Firestore sob o documento do usuário
            usersCollection.document(uid).update("profileImageUrl", downloadUrl).await()

            downloadUrl
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            null
        }
    }

    // Recupera o URL da imagem de perfil a partir do Firestore
    suspend fun getProfileImageUrl(): String? {
        val uid = getCurrentUserUid() ?: return null

        return try {
            // Tenta obter o documento do usuário no Firestore
            val userDoc = usersCollection.document(uid).get().await()

            // Verifica se o campo profileImageUrl existe no Firestore
            userDoc.getString("profileImageUrl")
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            null
        }
    }
}
