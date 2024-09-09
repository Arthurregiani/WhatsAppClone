package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityChatBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupUI()
        setupObservers()

        chatViewModel.checkOrCreateChat(getContactEmail())
    }

    // Configura o ViewModel com repositórios e dados do contato
    private fun setupViewModel() {
        val chatRepository = ChatRepository()
        val userRepository = UserRepository(FirebaseAuth.getInstance())
        val factory = ChatViewModelFactory(
            chatRepository,
            userRepository,
            getContactName(),
            getContactProfileImageUrl()
        )
        chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)
    }

    // Configura a interface do usuário, incluindo RecyclerView e botão de enviar mensagem
    private fun setupUI() {
        chatAdapter = ChatAdapter(emptyList())
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        configureToolbar()

        binding.buttonSend.setOnClickListener {
            sendMessage(getContactEmail())
        }
    }

    // Configura observadores para mudanças no ViewModel
    private fun setupObservers() {
        chatViewModel.messages.observe(this, Observer { messages ->
            val sortedMessages = messages.sortedBy { it.timestamp }
            chatAdapter.updateMessages(sortedMessages)
            binding.recyclerViewMessages.scrollToPosition(sortedMessages.size - 1)
        })

        chatViewModel.chatId.observe(this, Observer { chatId ->
            chatId?.let {
                chatViewModel.observeMessages(it)
            }
        })

        chatViewModel.contactName.observe(this, Observer { name ->
            binding.textContactName.text = name
        })

        chatViewModel.contactProfileImageUrl.observe(this, Observer { imageUrl ->
            Glide.with(this)
                .load(imageUrl ?: R.drawable.user_image_default)
                .into(binding.contactProfileImage)
        })
    }

    // Configura a toolbar
    private fun configureToolbar() {
        setSupportActionBar(binding.toolbarPrincipal.toolbarPrincipal)
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    // Envia uma mensagem para o chat
    private fun sendMessage(contactEmail: String) {
        val messageText = binding.editTextSend.text.toString().trim()
        if (messageText.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                chatViewModel.chatId.value?.let { chatId ->
                    chatViewModel.sendMessage(
                        chatId,
                        messageText,
                        chatViewModel.userRepository.getCurrentUserUid() ?: return@launch
                    )
                    clearInputField()
                }
            }
        }
    }

    // Limpa o campo de entrada de mensagem
    private fun clearInputField() {
        runOnUiThread {
            binding.editTextSend.text.clear()
        }
    }

    // Métodos para obter dados do Intent
    private fun getContactName() = intent.getStringExtra("contactName") ?: ""
    private fun getContactProfileImageUrl() = intent.getStringExtra("contactProfileImageUrl")
    private fun getContactEmail() = intent.getStringExtra("contactEmail") ?: ""

}
