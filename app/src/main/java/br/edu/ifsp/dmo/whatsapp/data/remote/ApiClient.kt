package br.edu.ifsp.dmo.whatsapp.data.remote

import br.edu.ifsp.dmo.whatsapp.BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiException(val status: Int, message: String) : IOException(message)

class ApiClient(private val session: SessionStore) {
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS).callTimeout(30, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS).build()

    suspend fun request(method: String, path: String, body: JSONObject? = null, auth: Boolean = true): JSONObject = withContext(Dispatchers.IO) {
        val token = if (auth) session.token else null
        val builder = Request.Builder().url(BuildConfig.API_BASE_URL + "/v1" + path)
        token?.let { builder.header("Authorization", "Bearer $it") }
        val requestBody = if (method == "GET") null else (body ?: JSONObject()).toString().toRequestBody("application/json".toMediaType())
        val call = client.newCall(builder.method(method, requestBody).build())
        try {
            call.execute().use { response ->
                val result = JSONObject(response.body?.string() ?: "{}")
                if (!response.isSuccessful) {
                    if (auth && response.code == 401 && session.token == token) session.clear()
                    throw ApiException(response.code, result.optString("error", "Não foi possível concluir a operação."))
                }
                result
            }
        } catch (error: ApiException) { throw error }
        catch (_: IOException) { throw IOException("Sem conexão com o serviço. Verifique a internet e tente novamente.") }
    }

    /** Each collector owns a socket. repeatOnLifecycle closes it when the screen stops. */
    fun events(): Flow<Unit> = callbackFlow {
        var socket: WebSocket? = null
        val job = launch(Dispatchers.IO) {
            var backoff = 1_000L
            while (isActive && session.token != null) {
                val token = session.token ?: break
                val ended = CompletableDeferred<Unit>()
                socket = client.newWebSocket(Request.Builder().url(BuildConfig.API_BASE_URL + "/v1/events")
                    .header("Authorization", "Bearer $token").build(), object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) { backoff = 1_000; trySend(Unit) }
                    override fun onMessage(webSocket: WebSocket, text: String) { trySend(Unit) }
                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) { ended.complete(Unit) }
                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, reason); ended.complete(Unit) }
                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        if (response?.code == 401 && session.token == token) session.clear()
                        ended.complete(Unit)
                    }
                })
                try { ended.await() } finally { socket?.cancel() }
                delay(backoff)
                backoff = (backoff * 2).coerceAtMost(30_000)
            }
        }
        trySend(Unit)
        awaitClose { job.cancel(); socket?.cancel() }
    }.buffer(Channel.CONFLATED)
}
