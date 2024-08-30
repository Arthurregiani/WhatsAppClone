package br.edu.ifsp.dmo.whatsapp.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


// Inicializa o gerenciador de permissões com a activity que está solicitando permissões
// e uma interface de callback para notificar o resultado.
class PermissionManager(
    private val activity: Activity,
    private val callback: PermissionCallback
) {

    //Define um método onPermissionResult que é chamado quando o resultado das permissões é recebido.
    //Recebe o requestCode da solicitação, uma lista de permissões concedidas e uma lista de permissões negadas.
    interface PermissionCallback {
        fun onPermissionResult(
            requestCode: Int,
            grantedPermissions:
            List<String>,
            deniedPermissions: List<String>)
    }


    fun requestPermission(requestCode: Int, vararg permissions: String) {

        // Filtra permissões que ainda não foram concedidas
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }
        // Se houver permissões que precisam ser solicitadas, faça a solicitação
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissionsNeeded.toTypedArray(), requestCode)
        } else {
            //Se todas as permissões já foram concedidas, notifica o resultado através da callback.
            callback.onPermissionResult(requestCode, permissions.toList(), emptyList())
        }
    }


    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // Listas para armazenar permissões concedidas e negadas
        val grantedPermissions = mutableListOf<String>()
        val deniedPermissions = mutableListOf<String>()

        // Itera sobre os resultados da solicitação
        for ((index, permission) in permissions.withIndex()) {
            if (grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                // Adiciona permissões concedidas à lista
                grantedPermissions.add(permission)
            } else {
                // Adiciona permissões negadas à lista
                deniedPermissions.add(permission)
            }
        }
        // Notifica o resultado da solicitação de permissões
        callback.onPermissionResult(requestCode, grantedPermissions, deniedPermissions)
    }
}
