package br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.databinding.ItemContactBinding
import com.bumptech.glide.Glide

class ContactAdapter(private val onContactClick: (Contact) -> Unit) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    // Lista de contatos exibidos pelo adapter
    private var contacts: List<Contact> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        // Infla o layout do item de contato
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact)
        // Configura o clique no item
        holder.itemView.setOnClickListener {
            onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    // Atualiza a lista de contatos e notifica o RecyclerView sobre as mudanças
    fun submitList(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Vincula os dados do contato aos elementos da interface
        fun bind(contact: Contact) {
            binding.textContactName.text = contact.nome
            binding.textContactEmail.text = contact.email

            // Carrega a imagem de perfil usando Glide
            Glide.with(itemView.context)
                .load(contact.profileImageUrl ?: R.drawable.user_image_default) // Usa uma imagem padrão se a URL for nula
                .into(binding.contactProfileImage)
        }
    }
}
