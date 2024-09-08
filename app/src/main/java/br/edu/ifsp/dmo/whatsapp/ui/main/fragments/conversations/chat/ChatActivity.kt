package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityChatBinding
import com.bumptech.glide.Glide

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recebe os dados do contato do Intent
        val contactName = intent.getStringExtra("contactName") ?: ""
        val contactProfileImageUrl = intent.getStringExtra("contactProfileImageUrl")

        // Cria a ViewModel usando a Factory
        val factory = ChatViewModelFactory(contactName, contactProfileImageUrl)
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        setupUI()

        // Lógica para enviar a mensagem ao clicar no botão de envio
        binding.floatingActionButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun setupUI() {
        configureToolbar()

        // Observar o nome do contato e atualizar a UI
        chatViewModel.contactName.observe(this, Observer { name ->
            binding.textContactName.text = name
        })

        // Observar a URL da imagem de perfil e carregar a imagem
        chatViewModel.contactProfileImageUrl.observe(this, Observer { imageUrl ->
            Glide.with(this)
                .load(imageUrl.ifEmpty { R.drawable.user_image_default }) // Imagem padrão se URL for vazia
                .into(binding.contactProfileImage)
        })
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.toolbarPrincipal.toolbarPrincipal)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun sendMessage() {
        val message = binding.editTextSend.text.toString().trim()

        if (message.isNotEmpty()) {
            // Aqui você pode implementar o envio da mensagem (Firebase, Realtime Database, etc.)
            println("Mensagem enviada: $message")
            binding.editTextSend.text.clear() // Limpa o campo de texto após o envio
        }
    }
}
