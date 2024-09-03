package br.edu.ifsp.dmo.whatsapp.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.edu.ifsp.dmo.whatsapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserRepository(private val auth: FirebaseAuth) {

    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _authStatus = MutableLiveData<Boolean>()
    val authStatus: LiveData<Boolean> get() = _authStatus

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _authStatus.value = firebaseAuth.currentUser != null
        }
    }

    fun cadastrarAutenticacaoUsuario(email: String, password: String, callback: (Boolean, String?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    callback(true, uid, null)
                } else {
                    callback(false, null, task.exception?.message)
                }
            }
    }

    fun cadastrarUsuarioDatabase(uid: String, user: User) {
        database.child(uid).setValue(user)
    }

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

    fun logout() {
        auth.signOut()
    }

    fun getDataCurrentUser(callback: (User?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            database.child(uid).get()
                .addOnSuccessListener { dataSnapshot ->
                    val user = dataSnapshot.getValue(User::class.java)
                    callback(user)
                }
                .addOnFailureListener { exception ->
                    Log.e("UserRepository", "Erro ao obter dados do usuário: ${exception.message}")
                    callback(null)
                }
        } else {
            callback(null)
        }
    }

    fun uploadProfileName(name: String) {
        val uid = auth.currentUser?.uid
        uid?.let {
            database.child(it).child("nome").setValue(name)
        }
    }
}
