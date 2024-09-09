package br.edu.ifsp.dmo.whatsapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo.whatsapp.ui.registration.RegistrationActivity
import br.edu.ifsp.dmo.whatsapp.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    // Binding para acessar os elementos da interface
    private lateinit var binding: ActivityLoginBinding

    // ViewModel para gerenciar a autenticação
    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(UserRepository(FirebaseAuth.getInstance()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura o binding da interface
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observa o status de autenticação e navega para a MainActivity se autenticado
        loginViewModel.authStatus.observe(this) { isAuthenticated ->
            if (isAuthenticated) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Configura o clique do botão de login
        binding.buttonLogin.setOnClickListener {
            val email = binding.editLoginEmail.text.toString()
            val password = binding.editLoginSenha.text.toString()
            if (validarEntradaDados(email, password)) {
                loginViewModel.autenticarUsuario(email, password)
            }
        }

        // Configura o clique para navegar até a tela de registro
        binding.textViewCadastro.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }

    // Valida os dados de entrada do usuário
    private fun validarEntradaDados(email: String, senha: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            !email.matches(emailPattern.toRegex()) -> {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                false
            }
            senha.length < 6 -> {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
