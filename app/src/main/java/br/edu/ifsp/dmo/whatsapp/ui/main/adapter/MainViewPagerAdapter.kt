package br.edu.ifsp.dmo.whatsapp.ui.main.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts.FragmentContacts
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.FragmentConversations

class MainViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2 // Número de abas
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentConversations() // Fragmento da aba "Conversas"
            1 -> FragmentContacts()      // Fragmento da aba "Contatos"
            else -> throw IllegalStateException("Posição desconhecida: $position")
        }
    }
}
