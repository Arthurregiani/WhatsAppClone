package br.edu.ifsp.dmo.whatsapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import br.edu.ifsp.dmo.whatsapp.ui.ClassicActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityMainBinding
import br.edu.ifsp.dmo.whatsapp.ui.settings.SettingsActivity
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : ClassicActivity() {

    // Binding para a ActivityMainBinding
    private lateinit var binding: ActivityMainBinding

    // ViewModel para gerenciar o estado da Activity
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(UserRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observar mudanças no estado de logout
        mainViewModel.logoutStatus.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                abrirLogin()
                finish() // Finaliza a Activity atual
            }
        }

        // Configurações iniciais
        configureToolbar()
        configureViewPager()
    }

    // Inicia a Activity de login
    private fun abrirLogin() {
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    // Inicia a Activity de configurações
    private fun abrirConfiguracoes() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    // Configura a Toolbar da Activity
    private fun configureToolbar() {
        val toolbar = binding.toolbarPrincipal.toolbarPrincipal
        toolbar.setTitle("WhatsApp 2")
        setSupportActionBar(toolbar)
    }

    // Configura o ViewPager e TabLayout
    private fun configureViewPager() {
        // Adapter para o ViewPager2
        val adapter = MainViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Conecta o TabLayout ao ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Conversas"
                1 -> "Contatos"
                else -> null
            }
        }.attach()
    }

    // Infla o menu de opções
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val search = menu?.findItem(R.id.menuPesquisa)?.actionView as? androidx.appcompat.widget.SearchView
        search?.queryHint = "Pesquisar conversas e contatos"
        search?.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { search.clearFocus(); return true }
            override fun onQueryTextChange(query: String?): Boolean { mainViewModel.query.value = query.orEmpty(); return true }
        })
        return true
    }

    // Lida com a seleção de itens do menu
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuLogout -> {
                logout() // Chama a função de logout
                true
            }
            R.id.menuSettings -> {
                abrirConfiguracoes() // Abre configurações
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Função para realizar o logout
    private fun logout() {
        mainViewModel.logout()
    }
}
