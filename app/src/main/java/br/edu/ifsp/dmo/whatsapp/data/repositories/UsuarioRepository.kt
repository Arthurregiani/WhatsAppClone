package br.edu.ifsp.dmo.whatsapp.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import com.google.firebase.auth.FirebaseAuth

class UsuarioRepository(private val auth: FirebaseAuth) {

    // LiveData para monitorar o estado de autenticação
    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        // Observa mudanças no estado de autenticação
        auth.addAuthStateListener { firebaseAuth ->
            _authStatus.value = firebaseAuth.currentUser != null
        }
    }

    fun cadastrarUsuario(usuario: Usuario, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(usuario.email, usuario.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)  // Cadastro bem-sucedido
                } else {
                    callback(false, task.exception?.message)  // Falha no cadastro
                }
            }
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
