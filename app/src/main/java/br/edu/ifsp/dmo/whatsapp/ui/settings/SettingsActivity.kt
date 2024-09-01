package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.ImageRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import br.edu.ifsp.dmo.whatsapp.utils.PermissionManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private lateinit var permissionManager: PermissionManager
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(ImageRepository(FirebaseAuth.getInstance()))
    }

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 100
        const val REQUEST_IMAGE_PICK = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager = PermissionManager(this) { requestCode, grantedPermissions, deniedPermissions ->
//            grantedPermissions.forEach {
//                Toast.makeText(this, "Permissão concedida: $it", Toast.LENGTH_SHORT).show()
//            }
            deniedPermissions.forEach {
                Toast.makeText(this, "Permissão negada: $it", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissions()
        configureToolbar()
        configureButtons()
        observeViewModel()
    }

    private fun configureButtons() {
        binding.imageButtonGalery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
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
            val imageUri: Uri? = data?.data
            if (requestCode == REQUEST_IMAGE_CAPTURE && data?.extras?.get("data") is Bitmap) {
                val bitmap = data.extras?.get("data") as Bitmap
                val tempUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, bitmap, "temp", null))
                uploadImage(tempUri)
            } else if (requestCode == REQUEST_IMAGE_PICK && imageUri != null) {
                uploadImage(imageUri)
            }
        }
    }

    private fun uploadImage(imageUri: Uri) {
        viewModel.uploadProfileImage(imageUri)
    }

    private fun observeViewModel() {
        viewModel.profileImageUri.observe(this) { uri ->
            uri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.user_image_default) // Placeholder image
                    .into(binding.profileImage)
            }
        }

        viewModel.uploadStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Imagem de perfil atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Falha ao atualizar a imagem de perfil.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermissions() {
        permissionManager.requestPermission(
            requestCode = 1,
            permissions = requiredPermissions
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.handlePermissionsResult(requestCode, permissions, grantResults)
    }

    private fun configureToolbar() {
        val toolbar = binding.toolbarPrincipal.toolbarPrincipal
        toolbar.title = "Configurações"
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
