package br.edu.ifsp.dmo.whatsapp.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: Activity,
    private val callback: (Boolean) -> Unit = {}
) {

    fun requestPermission(requestCode: Int, vararg permissions: String) {
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            activity.requestPermissions(permissionsNeeded.toTypedArray(), requestCode)
        } else {
            callback(true) // Permissões já concedidas
        }
    }

    fun handlePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val deniedPermissions = permissions.filterIndexed { index, _ ->
            grantResults[index] != PackageManager.PERMISSION_GRANTED
        }

        callback(deniedPermissions.isEmpty()) // Retorna true se todas as permissões foram concedidas

        if (deniedPermissions.isNotEmpty()) {
            showPermissionDeniedDialog()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permissões Necessárias")
            .setMessage("Algumas permissões foram negadas. A aplicação não pode continuar sem essas permissões.")
            .setPositiveButton("OK") { _, _ -> activity.finish() }
            .show()
    }
}
