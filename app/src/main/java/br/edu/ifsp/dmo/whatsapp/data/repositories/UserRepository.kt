package br.edu.ifsp.dmo.whatsapp.data.repositories

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository(private val auth: FirebaseAuth) {

    // Instâncias do Firestore e Storage, e referência para a coleção de usuários
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    private val usersCollection = firestore.collection("users")

    // LiveData para acompanhar o status de autenticação
    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        // Atualiza o status de autenticação ao ouvir mudanças
        auth.addAuthStateListener { firebaseAuth ->
            _authStatus.value = firebaseAuth.currentUser != null
        }
    }

    // Obtém o UID do usuário atual
    fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    // Manipula falhas de operação
    private fun handleFailure(tag: String, e: Exception) {
        Log.e(tag, "Erro: ${e.message}")
    }

    // Cadastra um novo usuário e autentica com e-mail e senha
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

    // Cadastra o usuário no Firestore
    fun cadastrarUsuarioDatabase(uid: String, user: User) {
        val userDocument = usersCollection.document(uid)
        userDocument.set(user)
            .addOnSuccessListener {
                Log.d("UserRepository", "Usuário cadastrado com sucesso no Firestore.")
            }
            .addOnFailureListener { e ->
                handleFailure("UserRepository", e)
            }
    }

    // Valida a autenticação do usuário com e-mail e senha
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

    // Faz logout do usuário
    fun logout() {
        auth.signOut()
    }

    // Obtém os dados do usuário atual
    fun getDataCurrentUser(callback: (User?) -> Unit) {
        getCurrentUserUid()?.let { uid ->
            val userDocument = usersCollection.document(uid)
            userDocument.get()
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

    // Atualiza o nome do perfil do usuário atual
    fun uploadProfileName(name: String) {
        getCurrentUserUid()?.let { uid ->
            val userDocument = usersCollection.document(uid)
            userDocument.update("nome", name)
                .addOnSuccessListener {
                    Log.d("UserRepository", "Nome atualizado com sucesso no Firestore.")
                }
                .addOnFailureListener { e ->
                    handleFailure("UserRepository", e)
                }
        }
    }

    // Faz upload da imagem de perfil do usuário e retorna a URL
    suspend fun uploadProfileImage(imageUri: Uri): String? {
        val uid = getCurrentUserUid() ?: return null
        val profileImageRef = storageRef.child("profile_images/$uid.jpg")

        return try {
            val downloadUrl = profileImageRef.putFile(imageUri).await().storage.downloadUrl.await().toString()
            val userDocument = usersCollection.document(uid)
            userDocument.update("profileImageUrl", downloadUrl).await()
            downloadUrl
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            null
        }
    }

    // Obtém a URL da imagem de perfil do usuário atual
    suspend fun getProfileImageUrl(): String? {
        val uid = getCurrentUserUid() ?: return null

        return try {
            val userDoc = usersCollection.document(uid).get().await()
            userDoc.getString("profileImageUrl")
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            null
        }
    }

    // Obtém o UID do usuário baseado no e-mail fornecido
    suspend fun getUidByEmail(email: String): String? {
        return try {
            val querySnapshot = usersCollection.whereEqualTo("email", email).get().await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents.firstOrNull()?.id
            } else {
                null
            }
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            null
        }
    }

    // Adiciona um contato à lista de contatos do usuário atual
    suspend fun addContact(contactEmail: String): Boolean {
        val uid = getCurrentUserUid() ?: return false

        return try {
            val contactUid = getUidByEmail(contactEmail)
            if (contactUid != null) {
                val contactData = usersCollection.document(contactUid).get().await()
                val contactName = contactData.getString("nome") ?: "Nome desconhecido"

                val userDocument = usersCollection.document(uid)
                val contactInfo = mapOf(
                    "nome" to contactName,
                    "email" to contactEmail
                )
                userDocument.update("contacts.$contactUid", contactInfo).await()

                Log.d("UserRepository", "Contato adicionado com sucesso.")
                true
            } else {
                Log.d("UserRepository", "Contato não encontrado.")
                false
            }
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            false
        }
    }

    // Obtém a lista de contatos do usuário atual
    suspend fun getContacts(): List<Contact> {
        val uid = getCurrentUserUid() ?: return emptyList()

        return try {
            val userDocument = usersCollection.document(uid).get().await()
            val contactsMap = userDocument.get("contacts") as? Map<String, Map<String, String>>
            contactsMap?.map { (contactUid, contactInfo) ->
                val contactName = contactInfo["nome"] ?: "Nome desconhecido"
                val contactEmail = contactInfo["email"] ?: ""
                val profileImageUrl = usersCollection.document(contactUid).get().await().getString("profileImageUrl")
                Contact(nome = contactName, email = contactEmail, profileImageUrl = profileImageUrl)
            } ?: emptyList()
        } catch (e: Exception) {
            handleFailure("UserRepository", e)
            emptyList()
        }
    }
}
