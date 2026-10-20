package br.edu.ifsp.dmo.whatsapp.ui.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import br.edu.ifsp.dmo.whatsapp.ui.ClassicActivity
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityCadastroBinding
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository

class RegistrationActivity : ClassicActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar o ViewModel com uma Factory
        val viewModelFactory = RegistrationViewModelFactory(UserRepository())
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

        viewModel.busy.observe(this) { binding.buttonCadastrar.isEnabled = !it }
        setClickListeners()
    }

    private fun setClickListeners() {
        // Configurar o listener para o botão de cadastro
        binding.buttonCadastrar.setOnClickListener {
            val userName = binding.editTextNome.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val senha = binding.editTextSenha.text.toString()

            if (validarDados(userName, email, senha)) {
                // Chamar o método de cadastro no ViewModel
                viewModel.cadastrarUsuario(userName, email, senha)
            }
        }
    }

    private fun abrirTelaLogin() {
        // Abrir a tela de login e finalizar a atividade atual
        val intent = Intent(this, br.edu.ifsp.dmo.whatsapp.ui.main.MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun validarDados(userName: String, email: String, senha: String): Boolean {

        return when {
            userName.isBlank() -> {
                // Mostrar mensagem de erro se o nome estiver vazio
                Toast.makeText(this, "Nome inválido", Toast.LENGTH_SHORT).show()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                // Mostrar mensagem de erro se o email estiver inválido
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show()
                false
            }
            senha.length < 8 -> {
                // Mostrar mensagem de erro se a senha for muito curta
                Toast.makeText(this, "A senha deve ter pelo menos 8 caracteres", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
