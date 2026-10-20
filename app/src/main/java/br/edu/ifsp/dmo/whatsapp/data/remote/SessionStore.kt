package br.edu.ifsp.dmo.whatsapp.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Only encrypted session data reaches disk; the key never leaves Android Keystore. */
class SessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val alias = "whatsapp-clone-session-v2"
    @Volatile private var data: JSONObject? = read()
    val authenticated = MutableLiveData(data != null)
    val token: String? get() = data?.optString("token")?.takeIf { it.isNotEmpty() }
    val userId: String? get() = data?.optJSONObject("user")?.optString("id")

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    private fun read(): JSONObject? = try {
        val saved = preferences.getString("encrypted", null)
        if (saved == null) null else {
            val parts = saved.split(":")
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            JSONObject(String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8))
                .takeIf { it.optLong("expiresAt") > System.currentTimeMillis() }
        }
    } catch (_: Exception) {
        preferences.edit().clear().apply()
        null
    }

    fun save(value: JSONObject) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(value.toString().toByteArray(Charsets.UTF_8))
        check(preferences.edit().putString("encrypted", Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)).commit())
        data = value
        authenticated.postValue(true)
    }

    fun clear() {
        data = null
        preferences.edit().clear().commit()
        authenticated.postValue(false)
    }
}
