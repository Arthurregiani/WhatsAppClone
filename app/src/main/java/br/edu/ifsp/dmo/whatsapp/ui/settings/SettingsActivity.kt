package br.edu.ifsp.dmo.whatsapp.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject
import br.edu.ifsp.dmo.whatsapp.ui.ClassicActivity
import br.edu.ifsp.dmo.whatsapp.R
import br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository
import br.edu.ifsp.dmo.whatsapp.databinding.ActivityConfiguracoesBinding
import com.bumptech.glide.Glide

class SettingsActivity : ClassicActivity() {

    private lateinit var binding: ActivityConfiguracoesBinding
    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(UserRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfiguracoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()

    }

    // Configura a interface do usuário
    private fun setupUI() {
        configureToolbar()
        configureButtons()
    }

    // Configura os botões e suas ações
    private fun configureButtons() {
        // Configura o botão para selecionar uma imagem da galeria
        binding.imageButtonGalery.setOnClickListener { pickImageLauncher.launch("image/*") }

        // Configura o botão para tirar uma foto
        binding.imageButtonCam.setOnClickListener {
            try { takePictureLauncher.launch(null) }
            catch (_: android.content.ActivityNotFoundException) { Toast.makeText(this, "Nenhum aplicativo de câmera disponível.", Toast.LENGTH_LONG).show() }
        }

        // Habilita o botão de renomear quando o EditText está em foco
        binding.editTextProfile.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) enableRenameButton()
        }
    }

    // Lançador para tirar uma foto
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            val file = java.io.File(cacheDir, "profile-camera.jpg")
            file.outputStream().use { output -> it.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, output) }
            viewModel.uploadProfileImage(Uri.fromFile(file))
        }
    }

    // Lançador para selecionar uma imagem da galeria
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.uploadProfileImage(it) } }

    // Habilita o botão de renomear
    private fun enableRenameButton() {
        binding.buttonRename.apply {
            setImageResource(R.drawable.ic_check)
            isClickable = true
            setOnClickListener {
                viewModel.uploadProfileName(binding.editTextProfile.text.toString())
            }
        }
    }

    // Configura os observadores para LiveData do ViewModel
    private fun setupObservers() {
        viewModel.profileImageUri.observe(this) { uri ->
            Glide.with(this)
                .load(uri ?: R.drawable.user_image_default)
                .into(binding.profileImage)
        }

        viewModel.error.observe(this) { message ->
            message?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show(); viewModel.error.value = null }
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

    // Reseta as configurações após renomear
    private fun resetRenameSettings() {
        closeKeyboard()
        binding.editTextProfile.clearFocus()
        binding.buttonRename.apply {
            setImageResource(R.drawable.ic_edit)
            isClickable = false
        }
    }

    // Fecha o teclado virtual
    private fun closeKeyboard() {
        val view = currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menu.add(0, 100, 0, "Sobre e privacidade")
        menu.add(0, 101, 1, "Excluir minha conta")
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean = when (item.itemId) {
        100 -> {
            androidx.appcompat.app.AlertDialog.Builder(this).setTitle("WhatsApp 2")
                .setMessage("Aplicativo independente, sem conexão com o WhatsApp oficial.\n\nAs mensagens ficam no servidor e não têm criptografia ponta a ponta. Não há notificações com o app fechado nem recuperação de senha. O e-mail de cadastro não é verificado.")
                .setPositiveButton("Entendi", null).show()
            true
        }
        101 -> { confirmAccountDeletion(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun confirmAccountDeletion() {
        val password = android.widget.EditText(this).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = "Confirme sua senha"
        }
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Excluir minha conta?")
            .setMessage("Sua conta, seus contatos e todas as conversas associadas serão apagados, inclusive o histórico compartilhado. Essa ação não pode ser desfeita.")
            .setView(password).setNegativeButton("Cancelar", null).setPositiveButton("Excluir", null).create()
        dialog.setOnShowListener {
            val button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                if (password.text.isNullOrEmpty()) { password.error = "Informe sua senha"; return@setOnClickListener }
                button.isEnabled = false
                lifecycleScope.launch {
                    try {
                        br.edu.ifsp.dmo.whatsapp.App.api.request("DELETE", "/me", JSONObject().put("password", password.text.toString()))
                        br.edu.ifsp.dmo.whatsapp.App.session.clear()
                        dialog.dismiss()
                        startActivity(android.content.Intent(this@SettingsActivity, br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity::class.java)
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    } catch (e: CancellationException) { throw e }
                    catch (e: Exception) { password.error = e.message }
                    finally { button.isEnabled = true }
                }
            }
        }
        dialog.show()
    }

    // Configura a Toolbar
    private fun configureToolbar() {
        val toolbar = binding.toolbarPrincipal.toolbarPrincipal
        toolbar.title = getString(R.string.configurations)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
