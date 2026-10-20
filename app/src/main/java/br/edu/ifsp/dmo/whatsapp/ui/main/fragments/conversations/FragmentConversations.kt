package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo.whatsapp.App
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.FragmentConversationsBinding
import br.edu.ifsp.dmo.whatsapp.ui.main.MainViewModel
import br.edu.ifsp.dmo.whatsapp.ui.main.MainViewModelFactory
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat.ChatActivity
import kotlinx.coroutines.launch

class FragmentConversations : Fragment() {
    private var _binding: FragmentConversationsBinding? = null
    private val model: ConversationsViewModel by viewModels()
    private val main: MainViewModel by activityViewModels { MainViewModelFactory(UserRepository()) }
    private lateinit var adapter: ConversationsAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return FragmentConversationsBinding.inflate(inflater, container, false).also { _binding = it }.root
    }
    override fun onViewCreated(view: View, state: Bundle?) {
        val binding = _binding!!
        adapter = ConversationsAdapter { chat ->
            startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("contactName", chat.contact.nome); putExtra("contactEmail", chat.contact.email)
                putExtra("contactProfileImageUrl", chat.contact.profileImageUrl)
            })
        }
        binding.conversations.layoutManager = LinearLayoutManager(requireContext())
        binding.conversations.adapter = adapter
        model.chats.observe(viewLifecycleOwner) { render() }
        main.query.observe(viewLifecycleOwner) { render() }
        model.error.observe(viewLifecycleOwner) { render() }
        binding.emptyState.setOnClickListener { viewLifecycleOwner.lifecycleScope.launch { model.refresh() } }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) { App.api.events().collect { model.refresh() } }
        }
    }
    private fun render() {
        val binding = _binding ?: return
        val query = main.query.value.orEmpty()
        val rows = model.chats.value.orEmpty().filter { it.contact.nome.contains(query, true) || it.contact.email.contains(query, true) || it.lastMessage.contains(query, true) }
        adapter.submitList(rows)
        binding.emptyState.text = model.error.value ?: if (query.isNotEmpty()) "Nenhuma conversa encontrada." else getString(R.string.no_conversations)
        binding.emptyState.visibility = if (rows.isEmpty() || model.error.value != null) View.VISIBLE else View.GONE
    }
    override fun onDestroyView() { _binding?.conversations?.adapter = null; _binding = null; super.onDestroyView() }
}
