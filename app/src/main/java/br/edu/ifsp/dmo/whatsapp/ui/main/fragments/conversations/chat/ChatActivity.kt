package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityChatBinding
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
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

        val chatRepository = ChatRepository()
        val userRepository = UserRepository(FirebaseAuth.getInstance())

        // Initialize adapter once
        chatAdapter = ChatAdapter(emptyList())
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        val contactName = intent.getStringExtra("contactName") ?: ""
        val contactProfileImageUrl = intent.getStringExtra("contactProfileImageUrl") ?: ""
        val contactEmail = intent.getStringExtra("contactEmail") ?: ""

        val factory = ChatViewModelFactory(chatRepository, userRepository, contactName, contactProfileImageUrl)
        chatViewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        setupUI()

        binding.buttonSend.setOnClickListener {
            sendMessage(contactEmail)
        }

        // Observe messages
        chatViewModel.messages.observe(this, Observer { messages ->
            val sortedMessages = messages.sortedBy { it.timestamp } // Ordena as mensagens pelo timestamp
            chatAdapter.updateMessages(sortedMessages)
            binding.recyclerViewMessages.scrollToPosition(sortedMessages.size - 1)
        })

        // Observe chatId and load messages
        chatViewModel.chatId.observe(this, Observer { chatId ->
            chatId?.let {
                chatViewModel.observeMessages(it) // Start observing messages when chatId is available
            }
        })
    }

    private fun setupUI() {
        configureToolbar()

        chatViewModel.contactName.observe(this, Observer { name ->
            binding.textContactName.text = name
        })

        chatViewModel.contactProfileImageUrl.observe(this, Observer { imageUrl ->
            Glide.with(this)
                .load(imageUrl.ifEmpty { R.drawable.user_image_default })
                .into(binding.contactProfileImage)
        })
    }

    private fun configureToolbar() {
        setSupportActionBar(binding.toolbarPrincipal.toolbarPrincipal)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun sendMessage(contactEmail: String) {
        val messageText = binding.editTextSend.text.toString().trim()

        if (messageText.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val contactUserId = chatViewModel.userRepository.getUidByEmail(contactEmail)
                val currentUserId = chatViewModel.userRepository.getCurrentUserUid()

                if (contactUserId != null && currentUserId != null) {
                    val participants = listOf(currentUserId, contactUserId)
                    val existingChatId = chatViewModel.chatId.value

                    if (existingChatId == null) {
                        chatViewModel.createChat(participants) { chatId ->
                            chatId?.let {
                                chatViewModel.sendMessage(it, messageText, currentUserId)
                                clearInputField()
                            }
                        }
                    } else {
                        chatViewModel.sendMessage(existingChatId, messageText, currentUserId)
                        clearInputField()
                    }
                }
            }
        }
    }

    private fun clearInputField() {
        runOnUiThread {
            binding.editTextSend.text.clear()
        }
    }
}
