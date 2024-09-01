package br.edu.ifsp.dmo.whatsapp.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: Activity,
    private val callback: (Int, List<String>, List<String>) -> Unit
) {

    fun requestPermission(requestCode: Int, vararg permissions: String) {
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(), requestCode)
        } else {
            callback(requestCode, permissions.toList(), emptyList())
        }
    }

    fun handlePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()

        for ((index, permission) in permissions.withIndex()) {
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permission)
            } else {
                deniedPermissions.add(permission)
            }
        }

        callback(requestCode, grantedPermissions, deniedPermissions)

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
