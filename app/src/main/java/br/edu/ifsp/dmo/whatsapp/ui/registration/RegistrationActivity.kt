package br.edu.ifsp.dmo.whatsapp.ui.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityCadastroBinding
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar o ViewModel com uma Factory
        val viewModelFactory = RegistrationViewModelFactory(UserRepository(FirebaseAuth.getInstance()))
        viewModel = ViewModelProvider(this, viewModelFactory)[RegistrationViewModel::class.java]

        // Observar o status do cadastro
        viewModel.cadastroStatus.observe(this) { result ->
            val (sucesso, mensagemErro) = result
            if (sucesso) {
                // Mostrar mensagem de sucesso e abrir a tela de login
                Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                abrirTelaLogin()
            } else {
                // Mostrar mensagem de erro
                Toast.makeText(this, "Falha no cadastro: $mensagemErro", Toast.LENGTH_SHORT).show()
            }
        }

        setClickListeners()
    }

    private fun setClickListeners() {
        // Configurar o listener para o botão de cadastro
        binding.buttonCadastrar.setOnClickListener {
            val userName = binding.editTextNome.text.toString()
            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()

            if (validarDados(userName, email, senha)) {
                // Chamar o método de cadastro no ViewModel
                viewModel.cadastrarUsuario(userName, email, senha)
            }
        }
    }

    private fun abrirTelaLogin() {
        // Abrir a tela de login e finalizar a atividade atual
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validarDados(userName: String, email: String, senha: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return when {
            userName.isEmpty() -> {
                // Mostrar mensagem de erro se o nome estiver vazio
                Toast.makeText(this, "Nome inválido", Toast.LENGTH_SHORT).show()
                false
            }
            !email.matches(emailPattern.toRegex()) -> {
                // Mostrar mensagem de erro se o email estiver inválido
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                false
            }
            senha.length < 6 -> {
                // Mostrar mensagem de erro se a senha for muito curta
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
