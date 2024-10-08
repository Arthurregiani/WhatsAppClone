package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.FragmentContactsBinding
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat.ChatActivity
import com.google.firebase.auth.FirebaseAuth

class FragmentContacts : Fragment() {

    // View binding
    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!

    // ViewModel e Adapter
    private lateinit var contactViewModel: ContactViewModel
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Configuração do binding e ViewModel
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        val factory = ContactViewModelFactory(UserRepository(FirebaseAuth.getInstance()))
        contactViewModel = ViewModelProvider(this, factory)[ContactViewModel::class.java]

        // Configura o RecyclerView com o adapter e o layout manager
        contactAdapter = ContactAdapter { contact ->
            onContactClick(contact) // Ação ao clicar no contato
        }
        binding.RecyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.RecyclerViewContacts.adapter = contactAdapter

        // Obtém e exibe a lista de contatos
        getContacts()

        // Configura o botão para adicionar novos contatos
        binding.buttonAddContact.setOnClickListener {
            showAddContactDialog()
        }

        return binding.root
    }

    // Obtém a lista de contatos do ViewModel e atualiza o adapter
    private fun getContacts() {
        contactViewModel.getContacts { contacts ->
            contactAdapter.submitList(contacts)
        }
    }

    // Exibe um diálogo para adicionar um novo contato
    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contact, null)
        val emailEditText: EditText = dialogView.findViewById(R.id.editTextEmail)

        AlertDialog.Builder(requireContext())
            .setTitle("Adicionar Contato")
            .setView(dialogView)
            .setPositiveButton("Salvar") { _, _ ->
                val email = emailEditText.text.toString().trim()
                if (email.isNotEmpty()) {
                    contactViewModel.addContact(email) { success, errorMessage ->
                        if (success) {
                            Toast.makeText(requireContext(), "Contato adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                            getContacts() // Atualiza a lista de contatos
                        } else {
                            Toast.makeText(requireContext(), "Erro: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Por favor, insira um e-mail válido.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Ação ao clicar em um contato: abre a atividade de chat
    private fun onContactClick(contact: Contact) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra("contactName", contact.nome)
            putExtra("contactEmail", contact.email)
            putExtra("contactProfileImageUrl", contact.profileImageUrl)
        }
        startActivity(intent)
    }

    // Limpa o binding quando a view é destruída
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
