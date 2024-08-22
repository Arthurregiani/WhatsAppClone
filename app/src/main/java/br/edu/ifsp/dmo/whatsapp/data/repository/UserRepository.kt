package br.edu.ifsp.dmo.whatsapp.data.repository

import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


class UserRepository {

    private val database = Firebase.firestore
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun cadastrarUsuario(usuario: Usuario){

        // Cadastrar o usuário no Firebase Authentication
        auth.createUserWithEmailAndPassword(usuario.email, usuario.senha)

    }
}


