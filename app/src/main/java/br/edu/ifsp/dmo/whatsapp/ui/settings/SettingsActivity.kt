package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import br.edu.ifsp.dmo.whatsapp.utils.PermissionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private lateinit var permissionManager: PermissionManager


    // Lista de permissões necessárias
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager = PermissionManager(this)

        requestPermissions()
        configureImageProfile()
        configureToolbar()
    }

    // Solicita as permissões
    private fun requestPermissions() {
        permissionManager.requestPermission(
            requestCode = 1,
            permissions = requiredPermissions
        ) { grantedPermissions, deniedPermissions ->
            grantedPermissions.forEach {
                Toast.makeText(this, "Permissão concedida: $it", Toast.LENGTH_SHORT).show()
            }
            deniedPermissions.forEach {
                Toast.makeText(this, "Permissão negada: $it", Toast.LENGTH_SHORT).show()
            }
        }
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
        toolbar.title = "Configurações"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Lidar com os resultados das permissões
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(permissions, grantResults)
    }

}
