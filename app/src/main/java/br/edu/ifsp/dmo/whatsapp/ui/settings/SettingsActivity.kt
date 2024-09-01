package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
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

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 100
        const val REQUEST_IMAGE_PICK = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager = PermissionManager(this)

        requestPermissions()
        configureToolbar()
        configureImageProfile()
        configureButtons()
    }

    private fun configureButtons() {
        binding.imageButtonGalery.setOnClickListener {
            val galeryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galeryIntent, REQUEST_IMAGE_PICK)

        }

        binding.imageButtonCam.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            var imageBitmap: android.graphics.Bitmap? = null
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                }

                REQUEST_IMAGE_CAPTURE -> {
                    imageBitmap = data?.extras?.get("data") as android.graphics.Bitmap
                }
            }
            if (imageBitmap != null) {
                binding.profileImage.setImageBitmap(imageBitmap)
            }

        }

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


//        val imageUrl = "" // URL da imagem
//        val placeholder = R.drawable.user_image_default // Imagem padrão enquanto carrega
//
//        Glide.with(this)
//            .load(imageUrl)
//            .apply(
//                RequestOptions()
//                    .placeholder(placeholder) // Exibe imagem padrão enquanto carrega
//                    .circleCrop() // Aplica o formato circular
//            )
//            .into(binding.profileImage)
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
