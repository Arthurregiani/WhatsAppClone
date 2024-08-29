package br.edu.ifsp.dmo.whatsapp.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.UsuarioRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityMainBinding
import br.edu.ifsp.dmo.whatsapp.ui.configuracoes.ConfiguracoesActivity
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity
import br.edu.ifsp.dmo.whatsapp.ui.main.adapter.MainViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(UsuarioRepository(FirebaseAuth.getInstance()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observar o estado de autenticação
        mainViewModel.logoutStatus.observe(this) { isLoggedOut ->
            if (isLoggedOut) {
                abrirLogin()
                finish()
            }
        }
        configureToolbar()
        configureViewPager()
    }
    private fun abrirLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun abrirConfiguracoes() {
        startActivity(Intent(this, ConfiguracoesActivity::class.java))
    }


    // Configurar a Toolbar
    private fun configureToolbar() {
        val toolbar = binding.toolbarPrincipal.toolbarPrincipal
        toolbar.setTitle("WhatsApp 2")
        setSupportActionBar(toolbar)
    }

    private fun configureViewPager() {
        // Configurar o adapter do ViewPager2
        val adapter = MainViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // Conectar o TabLayout ao ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Conversas"
                1 -> "Contatos"
                else -> null
            }
        }.attach()
    }

    // Inflar o menu de opções
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Lidar com itens do menu
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menuLogout -> {
                logout()
                true
            }
            R.id.menuSettings -> {
                abrirConfiguracoes()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    // Função para lidar com o clique no botão de logout
    private fun logout() {
        mainViewModel.logout()
    }
}
