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
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import br.edu.ifsp.dmo.whatsapp.utils.PermissionManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(UserRepository(FirebaseAuth.getInstance()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        requestPermissions()
    }

    private fun setupUI() {
        configureToolbar()
        configureButtons()
    }

    private fun configureButtons() {
        binding.imageButtonGalery.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.imageButtonCam.setOnClickListener { takePictureLauncher.launch(null) }
        binding.editTextProfile.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) enableRenameButton()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val tempUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, it, "temp", null))
            viewModel.uploadProfileImage(tempUri)
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.uploadProfileImage(it) } }

    private fun enableRenameButton() {
        binding.buttonRename.apply {
            setImageResource(R.drawable.ic_check)
            isClickable = true
            setOnClickListener {
                viewModel.uploadProfileName(binding.editTextProfile.text.toString())
            }
        }
    }

    private fun setupObservers() {
        viewModel.profileImageUri.observe(this) { uri ->
            Glide.with(this)
                .load(uri ?: R.drawable.user_image_default)
                .into(binding.profileImage)
        }

        viewModel.profileName.observe(this) { binding.editTextProfile.setText(it) }

        viewModel.uploadStatus.observe(this) { isSuccess ->
            Toast.makeText(this, if (isSuccess) "Imagem de perfil atualizada com sucesso!" else "Falha ao atualizar a imagem de perfil.", Toast.LENGTH_SHORT).show()
        }

        viewModel.uploadNameSuccess.observe(this) { isSuccess ->
            Toast.makeText(this, if (isSuccess) "Nome do perfil atualizado com sucesso!" else "Falha ao atualizar o nome do perfil.", Toast.LENGTH_SHORT).show()
            resetRenameSettings()
        }
    }

    private fun resetRenameSettings() {
        closeKeyboard()
        binding.editTextProfile.clearFocus()
        binding.buttonRename.apply {
            setImageResource(R.drawable.ic_edit)
            isClickable = false
        }
    }

    private fun closeKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    private fun requestPermissions() {
        PermissionManager(this) { allGranted ->
            if (!allGranted) {
                Toast.makeText(this, "Permissão negada.", Toast.LENGTH_SHORT).show()
            }
        }.requestPermission(1, android.Manifest.permission.CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager(this).handlePermissionsResult(requestCode, permissions, grantResults)
    }

    private fun configureToolbar() {
        val toolbar = binding.toolbarPrincipal.toolbarPrincipal
        toolbar.title = getString(R.string.configurations)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
