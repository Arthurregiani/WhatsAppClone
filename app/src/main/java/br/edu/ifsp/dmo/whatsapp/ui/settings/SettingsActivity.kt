package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import br.edu.ifsp.dmo.whatsapp.utils.PermissionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SettingsActivity : AppCompatActivity(), PermissionManager.PermissionCallback {

    private lateinit var binding: ActivityConfiguracoesBinding
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager = PermissionManager(this, this)

        requestPermissions()
        configureImageProfile()
        configureToolbar()
    }

    // Faz chamada ao método requestPermission do PermissionManager
    private fun requestPermissions() {
        permissionManager.requestPermission(
            PERMISSION_REQUEST_CODE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        )
    }

    // Lidar com os resultados das permissões solicitadas
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionResult(requestCode: Int, grantedPermissions: List<String>, deniedPermissions: List<String>) {
        if (grantedPermissions.isNotEmpty()) {
            for (permission in grantedPermissions) {
                showToast("Permissão concedida: $permission")
            }
        }
        if (deniedPermissions.isNotEmpty()) {
            for (permission in deniedPermissions) {
                showToast("Permissão negada: $permission")
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
        toolbar.setTitle("Configurações")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}
