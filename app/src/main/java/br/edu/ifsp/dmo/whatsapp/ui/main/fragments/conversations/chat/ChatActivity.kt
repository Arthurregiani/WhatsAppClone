package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import br.edu.ifsp.dmo.whatsapp.ui.ClassicActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo.whatsapp.App
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.ChatRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityChatBinding
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class ChatActivity : ClassicActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var model: ChatViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        model = ViewModelProvider(this, ChatViewModelFactory(ChatRepository(), UserRepository(),
            intent.getStringExtra("contactName") ?: "", intent.getStringExtra("contactProfileImageUrl")))[ChatViewModel::class.java]
        val adapter = ChatAdapter(emptyList())
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.recyclerViewMessages.adapter = adapter
        setSupportActionBar(binding.toolbarPrincipal.toolbarPrincipal)
        supportActionBar?.apply { setDisplayShowTitleEnabled(false); setDisplayHomeAsUpEnabled(true) }
        binding.textContactName.text = intent.getStringExtra("contactName")
        Glide.with(this).load(intent.getStringExtra("contactProfileImageUrl") ?: R.drawable.user_image_default).into(binding.contactProfileImage)
        binding.buttonSend.setOnClickListener {
            binding.editTextSend.text.toString().trim().takeIf { it.isNotEmpty() }?.let { model.sendMessage(it) }
        }
        binding.buttonOlder.setOnClickListener { model.older() }
        binding.buttonSend.isEnabled = false
        model.chatId.observe(this) { binding.buttonSend.isEnabled = it != null && model.sending.value != true }
        model.hasMore.observe(this) { binding.buttonOlder.visibility = if (it) View.VISIBLE else View.GONE }
        model.messages.observe(this) { messages ->
            val manager = binding.recyclerViewMessages.layoutManager as LinearLayoutManager
            val nearBottom = manager.findLastVisibleItemPosition() >= adapter.itemCount - 3
            adapter.updateMessages(messages)
            if (nearBottom && messages.isNotEmpty()) binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
        }
        model.sending.observe(this) { binding.buttonSend.isEnabled = !it && model.chatId.value != null }
        model.sent.observe(this) { text ->
            if (text != null) {
                if (binding.editTextSend.text.toString().trim() == text) binding.editTextSend.text.clear()
                model.sent.value = null
            }
        }
        model.error.observe(this) { message ->
            message?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show(); model.error.value = null }
        }
        App.session.authenticated.observe(this) {
            if (!it) startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                App.api.events().collect { model.refresh(intent.getStringExtra("contactEmail") ?: "") }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
