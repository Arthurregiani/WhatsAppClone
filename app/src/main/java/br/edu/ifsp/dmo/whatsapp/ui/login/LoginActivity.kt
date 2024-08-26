package br.edu.ifsp.dmo.whatsapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityLoginBinding
import br.edu.ifsp.dmo.whatsapp.ui.cadastro.CadastroActivity
import br.edu.ifsp.dmo.whatsapp.ui.main.MainActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
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

                //fazer login do usuario

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
    }

    private fun validarDados(email: String, senha: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (email.matches(emailPattern.toRegex())) {
            if (senha.isNotEmpty()) {
                return true
            } else {
                Toast.makeText(this, "Preencha a senha", Toast.LENGTH_SHORT).show()
                return false
            }
        } else {
            Toast.makeText(this, "Email invalido", Toast.LENGTH_SHORT).show()
            return false
        }
    }


}