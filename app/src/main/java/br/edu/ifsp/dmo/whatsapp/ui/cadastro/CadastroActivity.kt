package br.edu.ifsp.dmo.whatsapp.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityCadastroBinding
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity

class CadastroActivity : AppCompatActivity()  {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var cadastroViewModel: CadastroViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)

    }

    private fun setupListeners() {
        binding.buttonCadastrar.setOnClickListener {

            // Obter os dados de cadastro
            val userName = binding.TextInputNome.toString()
            val email = binding.TextInputEmail.toString()
            val senha = binding.textInputSenha.toString()

            //valida os dados de cadastro
            if (validarDados(userName, email, senha)) {

                // Cadastrar o usuário

                // Navegar para a tela de login

            }
        }
    }

    // Validar os dados de cadastro
    private fun validarDados(userName: String, email: String, senha: String): Boolean {

        // Verificar se os campos não estão vazios
        if (userName.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()) {
            return true
        }
        else{
            //exibir mensagem de erro se os campos não foram completos
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            return false
        }
    }
}
