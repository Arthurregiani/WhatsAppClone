package br.edu.ifsp.dmo.whatsapp.data.repositories

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import br.edu.ifsp.dmo.whatsapp.App
import br.edu.ifsp.dmo.whatsapp.data.model.Contact
import br.edu.ifsp.dmo.whatsapp.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class UserRepository {
    val authStatus get() = App.session.authenticated
    fun getCurrentUserUid(): String? = App.session.userId

    suspend fun register(nome: String, email: String, password: String) {
        val result = App.api.request("POST", "/auth/register", JSONObject().put("nome", nome).put("email", email).put("password", password), auth = false)
        withContext(Dispatchers.IO) { App.session.save(result) }
    }

    suspend fun login(email: String, password: String) {
        val result = App.api.request("POST", "/auth/login", JSONObject().put("email", email).put("password", password), auth = false)
        withContext(Dispatchers.IO) { App.session.save(result) }
    }

    suspend fun logout() {
        try { App.api.request("POST", "/auth/logout") }
        finally { withContext(Dispatchers.IO) { App.session.clear() } }
    }

    suspend fun currentUser(): User {
        val value = App.api.request("GET", "/me").getJSONObject("user")
        return User(value.getString("nome"), value.getString("email"), value.optString("profileImageUrl").takeUnless { it == "null" || it.isBlank() })
    }

    suspend fun uploadProfileName(name: String) {
        App.api.request("PATCH", "/me", JSONObject().put("nome", name))
    }

    suspend fun uploadProfileImage(imageUri: Uri): String = withContext(Dispatchers.IO) {
        val resolver = App.instance.contentResolver
        val bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, imageUri)) { decoder, info, _ ->
                val scale = 256f / maxOf(info.size.width, info.size.height).coerceAtLeast(256)
                decoder.setTargetSize((info.size.width * scale).toInt().coerceAtLeast(1), (info.size.height * scale).toInt().coerceAtLeast(1))
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            // Read bounds first to avoid decoding a full camera photograph on Android 8.
            val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(imageUri).use { android.graphics.BitmapFactory.decodeStream(it, null, bounds) }
            val options = android.graphics.BitmapFactory.Options().apply {
                var sample = 1
                while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 512) sample *= 2
                inSampleSize = sample
            }
            resolver.openInputStream(imageUri).use { android.graphics.BitmapFactory.decodeStream(it, null, options) }
                ?: throw IllegalArgumentException("Não foi possível abrir a imagem.")
        }
        val scale = 256f / maxOf(bitmap.width, bitmap.height).coerceAtLeast(256)
        val resized = Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt().coerceAtLeast(1), (bitmap.height * scale).toInt().coerceAtLeast(1), true)
        val bytes = ByteArrayOutputStream().also { resized.compress(Bitmap.CompressFormat.JPEG, 75, it) }.toByteArray()
        if (resized !== bitmap) resized.recycle()
        bitmap.recycle()
        val image = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        App.api.request("PUT", "/me/avatar", JSONObject().put("image", image)).getString("profileImageUrl")
    }

    suspend fun addContact(contactEmail: String): Boolean {
        App.api.request("POST", "/contacts", JSONObject().put("email", contactEmail))
        return true
    }

    suspend fun getContacts(): List<Contact> {
        val contacts = App.api.request("GET", "/contacts").getJSONArray("contacts")
        return (0 until contacts.length()).map { parseContact(contacts.getJSONObject(it)) }
    }

    companion object {
        fun parseContact(value: JSONObject) = Contact(value.getString("nome"), value.getString("email"),
            value.optString("profileImageUrl").takeUnless { it == "null" || it.isBlank() })
    }
}
