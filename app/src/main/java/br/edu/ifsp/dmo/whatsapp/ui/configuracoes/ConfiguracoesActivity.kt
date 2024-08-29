package br.edu.ifsp.dmo.whatsapp.ui.configuracoes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ConfiguracoesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureToolbar()
        configureImageProfile()
    }

    private fun configureImageProfile() {
        val imageUrl = "" // URL da imagem
        val placeholder = R.drawable.user_image_default // Imagem padrão enquanto carrega

        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions()
                .placeholder(placeholder) // Exibe imagem padrão enquanto carrega
                .circleCrop() // Aplica o formato circular
            )
            .into(binding.profileImage)
    }

    private fun configureToolbar() {
        val toolbar = binding.toolbarPrincipal.toolbarPrincipal
        toolbar.setTitle("Configurações")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
