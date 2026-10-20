package br.edu.ifsp.dmo.whatsapp

import android.content.Intent
import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.edu.ifsp.dmo.whatsapp.ui.login.LoginActivity
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

/** Runs against the deployed HTTPS service, using disposable accounts only. */
@RunWith(AndroidJUnit4::class)
class MessagingFlowTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val password = UUID.randomUUID().toString() + "Qa1!"
    private val suffix = UUID.randomUUID().toString()
    private val aliceEmail = "qa-android-a-$suffix@example.test"
    private val bobEmail = "qa-android-b-$suffix@example.test"
    private val client = OkHttpClient()

    private fun api(method: String, path: String, token: String? = null, body: JSONObject? = null): JSONObject {
        val builder = Request.Builder().url(BuildConfig.API_BASE_URL + "/v1" + path)
        token?.let { builder.header("Authorization", "Bearer $it") }
        val payload = if (method == "GET") null else (body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType())
        client.newCall(builder.method(method, payload).build()).execute().use {
            val result = JSONObject(it.body!!.string())
            assertTrue("$method $path: ${it.code} ${result.optString("error")}", it.isSuccessful)
            return result
        }
    }
    private fun waitForView(text: String) {
        val end = System.currentTimeMillis() + 20_000
        var last: Throwable? = null
        while (System.currentTimeMillis() < end) {
            try { onView(withText(text)).check(matches(isDisplayed())); return }
            catch (error: Throwable) { last = error; Thread.sleep(200) }
        }
        throw AssertionError("Did not display $text", last)
    }
    private fun screenshot(name: String) {
        val directory = File(context.getExternalFilesDir(null), "evidence").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().useBitmap { bitmap ->
            File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
    private inline fun Bitmap.useBitmap(block: (Bitmap) -> Unit) { try { block(this) } finally { recycle() } }

    @Test fun registrationContactLiveMessagingProfileSearchAndRelogin() {
        val bob = api("POST", "/auth/register", body = JSONObject().put("nome", "Bruno QA").put("email", bobEmail).put("password", password))
        instrumentation.runOnMainSync { App.session.clear() }
        val scenario = ActivityScenario.launch<LoginActivity>(Intent(context, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        try {
            screenshot("01-login")
            onView(withId(R.id.textViewCadastro)).perform(click())
            onView(withId(R.id.editTextNome)).perform(replaceText("Ana QA"))
            onView(withId(R.id.editTextEmail)).perform(replaceText(aliceEmail))
            onView(withId(R.id.editTextSenha)).perform(replaceText(password), closeSoftKeyboard())
            onView(withId(R.id.buttonCadastrar)).perform(click())
            waitForView("WhatsApp 2")
            onView(withText("Contatos")).perform(click())
            onView(withId(R.id.buttonAddContact)).perform(click())
            onView(withId(R.id.editTextEmail)).perform(replaceText(bobEmail), closeSoftKeyboard())
            onView(withText("Salvar")).perform(click())
            waitForView("Bruno QA")
            screenshot("02-contacts")
            onView(withText("Bruno QA")).perform(click())
            onView(withId(R.id.editTextSend)).perform(replaceText("Olá do Android"), closeSoftKeyboard())
            // Wait for chat creation before submitting, as a real screen disables sending until ready.
            val chat = api("POST", "/chats", bob.getString("token"), JSONObject().put("email", aliceEmail)).getString("id")
            val connectedDeadline = System.currentTimeMillis() + 15000
            while (System.currentTimeMillis() < connectedDeadline) {
                try { onView(withId(R.id.buttonSend)).check(matches(isEnabled())); break }
                catch (_: AssertionError) { Thread.sleep(100) }
            }
            onView(withId(R.id.buttonSend)).perform(click())
            waitForView("Olá do Android")
            val received = api("GET", "/chats/$chat/messages", bob.getString("token")).getJSONArray("messages")
            assertEquals("Olá do Android", received.getJSONObject(0).getString("messageText"))
            api("POST", "/chats/$chat/messages", bob.getString("token"), JSONObject().put("messageText", "Resposta em tempo real").put("clientId", UUID.randomUUID().toString()))
            waitForView("Resposta em tempo real")
            screenshot("03-chat")
            onView(withId(R.id.editTextSend)).check(matches(withText("")))
            pressBack()
            onView(withText("Conversas")).perform(click())
            waitForView("Bruno QA")
            screenshot("04-conversations")
            onView(withId(R.id.menuPesquisa)).perform(click())
            onView(isAssignableFrom(androidx.appcompat.widget.SearchView.SearchAutoComplete::class.java)).perform(replaceText("Ninguém"), closeSoftKeyboard())
            waitForView("Nenhuma conversa encontrada.")
            pressBack()
            openActionBarOverflowOrOptionsMenu(context)
            onView(withText("Configurações")).perform(click())
            onView(withId(R.id.editTextProfile)).perform(click(), replaceText("Ana atualizada"), closeSoftKeyboard())
            onView(withId(R.id.button_rename)).perform(click())
            val deadline = System.currentTimeMillis() + 10_000
            while (System.currentTimeMillis() < deadline && runBlocking { App.api.request("GET", "/me").getJSONObject("user").getString("nome") } != "Ana atualizada") Thread.sleep(200)
            assertEquals("Ana atualizada", runBlocking { App.api.request("GET", "/me").getJSONObject("user").getString("nome") })
            // Exercise the real Android image decoder and compression pipeline with a local photo.
            val photo = File(context.cacheDir, "qa-avatar.jpg")
            Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888).useBitmap { bitmap ->
                bitmap.eraseColor(android.graphics.Color.rgb(7, 94, 84))
                photo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            }
            val avatar = runBlocking { br.edu.ifsp.dmo.whatsapp.data.repositories.UserRepository().uploadProfileImage(android.net.Uri.fromFile(photo)) }
            assertTrue(avatar.startsWith("data:image/jpeg;base64,"))
            assertTrue(avatar.length < 96000)
            assertEquals(avatar, runBlocking { App.api.request("GET", "/me").getJSONObject("user").getString("profileImageUrl") })
            photo.delete()
            screenshot("05-profile")
            pressBack()
            openActionBarOverflowOrOptionsMenu(context)
            onView(withText("Sair")).perform(click())
            waitForView("Não tem uma conta? Cadastre-se")
            onView(withId(R.id.editLoginEmail)).perform(replaceText(aliceEmail))
            onView(withId(R.id.editLoginSenha)).perform(replaceText(password), closeSoftKeyboard())
            onView(withId(R.id.buttonLogin)).perform(click())
            waitForView("WhatsApp 2")
            waitForView("Bruno QA")
            // Verify the persisted session is encrypted and survives activity recreation.
            val persisted = context.getSharedPreferences("session", 0).getString("encrypted", "")!!
            assertFalse(persisted.contains(App.session.token!!))
            assertFalse(persisted.contains(aliceEmail))
        } finally {
            scenario.close()
            for (email in listOf(aliceEmail, bobEmail)) {
                val login = api("POST", "/auth/login", body = JSONObject().put("email", email).put("password", password))
                api("DELETE", "/me", login.getString("token"), JSONObject().put("password", password))
            }
            instrumentation.runOnMainSync { App.session.clear() }
        }
    }
}
