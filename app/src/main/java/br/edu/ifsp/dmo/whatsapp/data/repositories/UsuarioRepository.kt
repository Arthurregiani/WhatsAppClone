package br.edu.ifsp.dmo.whatsapp.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UsuarioRepository(private val auth: FirebaseAuth) {

    // Instância do Firebase Realtime Database
    private val database = FirebaseDatabase.getInstance()

    // LiveData para monitorar o estado de autenticação
    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        // Observa mudanças no estado de autenticação
        auth.addAuthStateListener { firebaseAuth ->
            _authStatus.value = firebaseAuth.currentUser != null
        }
    }

    // Cadastrar um novo usuário com autenticação
    fun cadastrarAutenticacaoUsuario(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Cadastro bem-sucedido
                    // retorna o uid do usuário autenticado
                    val uid = auth.currentUser?.uid
                    callback(true, uid, null)  // Cadastro bem-sucedido
                } else {
                    callback(false, null, task.exception?.message)  // Falha no cadastro
                }
            }
    }

    fun cadastrarUsuarioDatabase(uid: String, usuario: Usuario) {
        // Salvar apenas o nome e o email no Firebase Realtime Database
        // com base no UID do usuário autenticado
        val usuariosRef = database.getReference("users")
        usuariosRef.child(uid).setValue(usuario)
    }

    fun validarAutenticacao(email: String, senha: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)  // Autenticação bem-sucedida
                } else {
                    callback(false, task.exception?.message)  // Falha na autenticação
                }
            }
    }

    fun logout() {
        auth.signOut()
    }
}
