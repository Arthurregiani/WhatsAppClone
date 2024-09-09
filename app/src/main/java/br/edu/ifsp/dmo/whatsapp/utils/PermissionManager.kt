package br.edu.ifsp.dmo.whatsapp.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class PermissionManager(
    private val activity: Activity,
    private val callback: (Boolean) -> Unit = {}
) {

    /**
     * Solicita permissões ao usuário.
     *
     * @param requestCode Código de solicitação para identificar a permissão.
     * @param permissions Lista de permissões a serem solicitadas.
     */
    fun requestPermission(requestCode: Int, vararg permissions: String) {
        // Filtra as permissões que ainda não foram concedidas
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            // Solicita permissões que ainda não foram concedidas
            activity.requestPermissions(permissionsNeeded.toTypedArray(), requestCode)
        } else {
            // Se todas as permissões já foram concedidas, chama o callback com true
            callback(true)
        }
    }

    /**
     * Manipula o resultado da solicitação de permissões.
     *
     * @param requestCode Código de solicitação para identificar a permissão.
     * @param permissions Lista de permissões solicitadas.
     * @param grantResults Resultados da solicitação de permissões.
     */
    fun handlePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // Filtra permissões que foram negadas
        val deniedPermissions = permissions.filterIndexed { index, _ ->
            grantResults[index] != PackageManager.PERMISSION_GRANTED
        }

        // Chama o callback com true se todas as permissões foram concedidas, caso contrário, com false
        callback(deniedPermissions.isEmpty())

        if (deniedPermissions.isNotEmpty()) {
            // Mostra um diálogo se alguma permissão foi negada
            showPermissionDeniedDialog()
        }
    }

    /**
     * Exibe um diálogo informando que algumas permissões foram negadas.
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Permissões Necessárias")
            .setMessage("Algumas permissões foram negadas. A aplicação não pode continuar sem essas permissões.")
            .setPositiveButton("OK") { _, _ -> activity.finish() }
            .show()
    }
}
