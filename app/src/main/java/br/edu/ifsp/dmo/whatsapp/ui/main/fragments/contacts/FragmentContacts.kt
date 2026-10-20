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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.dmo.whatsapp.App
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.FragmentContactsBinding
import br.edu.ifsp.dmo.whatsapp.ui.main.MainViewModel
import br.edu.ifsp.dmo.whatsapp.ui.main.MainViewModelFactory
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.chat.ChatActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class FragmentContacts : Fragment() {
    private var _binding: FragmentContactsBinding? = null
    private val model: ContactViewModel by viewModels { ContactViewModelFactory(UserRepository()) }
    private val main: MainViewModel by activityViewModels { MainViewModelFactory(UserRepository()) }
    private lateinit var adapter: ContactAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        FragmentContactsBinding.inflate(inflater, container, false).also { _binding = it }.root
    override fun onViewCreated(view: View, state: Bundle?) {
        val binding = _binding!!
        adapter = ContactAdapter { contact ->
            startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("contactName", contact.nome); putExtra("contactEmail", contact.email)
                putExtra("contactProfileImageUrl", contact.profileImageUrl)
            })
        }
        binding.RecyclerViewContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.RecyclerViewContacts.adapter = adapter
        model.contacts.observe(viewLifecycleOwner) { render() }
        model.error.observe(viewLifecycleOwner) { render() }
        main.query.observe(viewLifecycleOwner) { render() }
        binding.buttonAddContact.setOnClickListener { addDialog() }
        binding.emptyState.setOnClickListener { viewLifecycleOwner.lifecycleScope.launch { model.refresh() } }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) { App.api.events().collect { model.refresh() } }
        }
    }
    private fun render() {
        val binding = _binding ?: return
        val query = main.query.value.orEmpty()
        val rows = model.contacts.value.orEmpty().filter { it.nome.contains(query, true) || it.email.contains(query, true) }
        adapter.submitList(rows)
        binding.emptyState.text = model.error.value ?: if (query.isNotEmpty()) "Nenhum contato encontrado." else getString(R.string.no_contacts)
        binding.emptyState.visibility = if (rows.isEmpty() || model.error.value != null) View.VISIBLE else View.GONE
    }
    private fun addDialog() {
        val content = layoutInflater.inflate(R.layout.dialog_add_contact, null)
        val field = content.findViewById<EditText>(R.id.editTextEmail)
        val dialog = AlertDialog.Builder(requireContext()).setTitle("Adicionar contato").setView(content)
            .setPositiveButton("Salvar", null).setNegativeButton("Cancelar", null).create()
        dialog.setOnShowListener {
            val save = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            save.setOnClickListener {
                val email = field.text.toString().trim()
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { field.error = "Informe um e-mail válido"; return@setOnClickListener }
                save.isEnabled = false
                viewLifecycleOwner.lifecycleScope.launch {
                    try { model.add(email); dialog.dismiss() }
                    catch (e: CancellationException) { throw e }
                    catch (e: Exception) { if (isAdded) Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show() }
                    finally { save.isEnabled = true }
                }
            }
        }
        dialog.show()
    }
    override fun onDestroyView() { _binding?.RecyclerViewContacts?.adapter = null; _binding = null; super.onDestroyView() }
}
