package br.edu.ifsp.dmo.whatsapp.data.repository


import br.edu.ifsp.dmo.whatsapp.data.model.Usuario
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


class UsuarioRepository {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun cadastrarUsuario(usuario: Usuario){
        // Cadastrar o usuário no Firebase Authentication
        auth.createUserWithEmailAndPassword(usuario.email, usuario.senha)

    }


    fun validarAutenticacao(email: String, senha: String) {
        // Logar o usuário no Firebase Authentication
        auth.signInWithEmailAndPassword(email, senha)

    }
}


