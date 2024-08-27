package br.edu.ifsp.dmo.whatsapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.data.repositories.UsuarioRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo.whatsapp.ui.cadastro.CadastroActivity
import br.edu.ifsp.dmo.whatsapp.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar o ViewModel com uma Factory se necessário
        val viewModelFactory = LoginViewModelFactory(UsuarioRepository(FirebaseAuth.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)

        // Observar o status da autenticação
        viewModel.authStatus.observe(this) { result ->
            result.onSuccess {
                // Autenticação bem-sucedida
                Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()
                abrirTelaPrincipal()
            }.onFailure { exception ->
                // Falha na autenticação
                Toast.makeText(this, "Falha no login: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        setClickListeners()
    }

    private fun setClickListeners() {
        binding.textViewCadastro.setOnClickListener {
            abrirTelaCadastro()
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editLoginEmail.text.toString()
            val senha = binding.editLoginSenha.text.toString()
            if (validarDados(email, senha)){
                // Fazer login do usuário
                viewModel.autenticarUsuario(email, senha)
            }
        }
    }

    private fun abrirTelaCadastro() {
        val intent = Intent(this, CadastroActivity::class.java)
        startActivity(intent)
    }

    private fun abrirTelaPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Fecha a LoginActivity para que o usuário não possa voltar a ela pressionando o botão de voltar
    }

    private fun validarDados(email: String, senha: String): Boolean {
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
