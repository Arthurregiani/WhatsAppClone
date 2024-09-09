package br.edu.ifsp.dmo.whatsapp.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.contacts.FragmentContacts
import br.edu.ifsp.dmo.whatsapp.ui.main.fragments.conversations.FragmentConversations

class MainViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Retorna o número total de abas
    override fun getItemCount(): Int {
        return 2 // Número de abas: "Conversas" e "Contatos"
    }

    // Cria e retorna o Fragmento baseado na posição
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentConversations()
            1 -> FragmentContacts()
            else -> throw IllegalStateException("Posição desconhecida: $position")
        }
    }
}
