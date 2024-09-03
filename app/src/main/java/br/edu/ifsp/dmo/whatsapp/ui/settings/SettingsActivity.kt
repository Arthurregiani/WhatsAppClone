package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.ImageRepository
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import br.edu.ifsp.dmo.whatsapp.utils.PermissionManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private lateinit var permissionManager: PermissionManager
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(ImageRepository(FirebaseAuth.getInstance()), UserRepository(FirebaseAuth.getInstance()))
    }

    private val requiredPermissions = arrayOf(
        android.Manifest.permission.CAMERA
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        permissionManager = PermissionManager(this) { _, _, deniedPermissions ->
            deniedPermissions.forEach {
                Toast.makeText(this, "Permissão negada: $it", Toast.LENGTH_SHORT).show()
            }
        }

        requestPermissions()
        configureToolbar()
        configureButtons()
        setupObservers()
    }



    private fun configureButtons() {
        binding.imageButtonGalery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.imageButtonCam.setOnClickListener {
            takePictureLauncher.launch(null)
        }

        // Configura o listener para o EditText
        binding.editTextProfile.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Altera o ícone do ImageButton e o habilita
                binding.buttonRename.apply {
                    setImageResource(R.drawable.ic_check)
                    isClickable = true
                    setOnClickListener {
                        val newName = binding.editTextProfile.text.toString()
                        viewModel.uploadProfileName(newName)
                    }
                }
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val tempUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, it, "temp", null))
            uploadImage(tempUri)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { uploadImage(it) }
    }


    private fun uploadImage(imageUri: Uri) {
        viewModel.uploadProfileImage(imageUri)
    }

    private fun setupObservers() {

        // Observe se profileImageUri foi atualizado
        viewModel.profileImageUri.observe(this) { uri ->
            uri?.let {
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.user_image_default) // Placeholder image
                    .into(binding.profileImage)
            }
            viewModel.profileName.observe(this){
                binding.editTextProfile.setText(it)
            }
        }

        // Observe se o upload da foto foi bem-sucedido
        viewModel.uploadStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Imagem de perfil atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Falha ao atualizar a imagem de perfil.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe se o upload do nome foi bem-sucedido
        viewModel.uploadNameSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Nome do perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Falha ao atualizar o nome do perfil.", Toast.LENGTH_SHORT).show()
            }
            closeKeyboard()
            resetRenameSettings()

        }

    }

    // Reseta o perfil de edição de nome
    private fun resetRenameSettings() {
        binding.editTextProfile.clearFocus()
        binding.buttonRename.apply {
            setImageResource(R.drawable.ic_edit)
            isClickable = false
        }
    }


    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
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
