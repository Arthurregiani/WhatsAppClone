package br.edu.ifsp.dmo.whatsapp.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityCadastroBinding
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var viewModel: CadastroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[CadastroViewModel::class.java]
        setClickListeners()
    }


    private fun setClickListeners() {
        binding.buttonCadastrar.setOnClickListener {
            // Obter os dados de cadastro
            val userName = binding.editTextNome.text.toString()
            val email = binding.editTextEmail.text.toString()
            val senha = binding.editTextSenha.text.toString()

            //valida os dados de cadastro
            if (validarDados(userName, email, senha)) {

                // Cadastrar o usuário
                viewModel.cadastrarUsuario(userName, email, senha)
                Toast.makeText(this, "Cadastro realizado com sucesso", Toast.LENGTH_SHORT).show()
                abrirTelaLogin()


            }
        }
    }

    private fun abrirTelaLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }


    // Validar os dados de cadastro
    private fun validarDados(userName: String, email: String, senha: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        // Verificar se os campos não estão vazios
        if (userName.isNotEmpty()) {
            if (email.matches(emailPattern.toRegex())) {
                if (senha.isNotEmpty()) {
                    return true
                } else {
                    Toast.makeText(this, "Preencha com a senha", Toast.LENGTH_SHORT).show()
                    return false
                }
            } else {
                Toast.makeText(this, "email invalido", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            //exibir mensagem de erro se os campos não foram completos
            Toast.makeText(this, "Nome invalido", Toast.LENGTH_SHORT).show()
            return false
        }
    }

}
