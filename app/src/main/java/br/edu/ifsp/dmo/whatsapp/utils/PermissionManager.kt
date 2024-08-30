package br.edu.ifsp.dmo.whatsapp.utils

import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {

    private var onPermissionsResult: ((grantedPermissions: List<String>, deniedPermissions: List<String>) -> Unit)? = null

    // Solicita as permissões necessárias e define o callback para lidar com os resultados
    fun requestPermission(
        requestCode: Int,
        permissions: Array<String>,
        onResult: (grantedPermissions: List<String>, deniedPermissions: List<String>) -> Unit
    ) {
        onPermissionsResult = onResult

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(), requestCode)
        } else {
            onPermissionsResult?.invoke(permissions.toList(), emptyList())
        }
    }

    // Lida com os resultados das permissões solicitadas
    fun handlePermissionsResult(
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()

        permissions.forEachIndexed { index, permission ->
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permission)
            } else {
                deniedPermissions.add(permission)
            }
        }

        if (deniedPermissions.isNotEmpty()) {
            showPermissionDeniedDialog()
        } else {
            onPermissionsResult?.invoke(grantedPermissions, deniedPermissions)
        }
    }

    // Exibe um diálogo informando o usuário sobre as permissões negadas e finaliza a activity
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permissões negadas")
            .setMessage("As permissões necessárias foram negadas. O aplicativo não pode continuar.")
            .setPositiveButton("OK") { _, _ ->
                activity.finish()
            }
            .setCancelable(false)
            .show()
    }
}
