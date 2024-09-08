package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityChatBinding
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cria a instância do UserRepository
        userRepository = UserRepository(FirebaseAuth.getInstance())

        // Recebe os dados do contato do Intent
        val contactName = intent.getStringExtra("contactName") ?: ""
        val contactProfileImageUrl = intent.getStringExtra("contactProfileImageUrl")
        val contactEmail = intent.getStringExtra("contactEmail") ?: ""

        // Cria a ViewModel usando a Factory
        val factory = ChatViewModelFactory(contactName, contactProfileImageUrl)
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        setupUI()

        binding.buttonSend.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                sendMessage(contactEmail)
            }
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
    }

    private suspend fun sendMessage(contactEmail: String) {
        val messageText = binding.editTextSend.text.toString().trim()

        if (messageText.isNotEmpty()) {
            // Obtemos o ID do contato e do usuário atual
            val contactUserId = userRepository.getUidByEmail(contactEmail)
            val currentUserId = userRepository.getCurrentUserUid()

            if (contactUserId != null && currentUserId != null) {
                val participants = listOf(currentUserId, contactUserId)

                // Verifica se o chat já existe
                val existingChatId = chatViewModel.chatId.value

                if (existingChatId == null) {
                    // Se o chat não foi criado ainda, cria o chat antes de enviar a mensagem
                    chatViewModel.createChat(participants) { chatId ->
                        if (chatId != null) {
                            chatViewModel.sendMessage(chatId, messageText, currentUserId)
                            runOnUiThread {
                                binding.editTextSend.text.clear() // Limpa o campo de texto após o envio
                            }
                        }
                    }
                } else {
                    // Se o chat já foi criado, envia a mensagem
                    chatViewModel.sendMessage(existingChatId, messageText, currentUserId)
                    runOnUiThread {
                        binding.editTextSend.text.clear() // Limpa o campo de texto após o envio
                    }
                }
            } else {
                // Lidar com o caso em que o ID do contato ou do usuário não é encontrado
            }
        }
    }
}
