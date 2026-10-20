package br.edu.ifsp.dmo.whatsapp.data.repositories

import br.edu.ifsp.dmo.whatsapp.App
import br.edu.ifsp.dmo.whatsapp.data.model.Chat
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import org.json.JSONObject

class ChatRepository {
    suspend fun getOrCreateChat(email: String): String = App.api.request("POST", "/chats", JSONObject().put("email", email)).getString("id")

    suspend fun sendMessage(chatId: String, messageText: String, clientId: String): Message {
        val result = App.api.request("POST", "/chats/$chatId/messages", JSONObject().put("messageText", messageText).put("clientId", clientId))
        return parseMessage(result.getJSONObject("message"))
    }

    suspend fun messages(chatId: String, before: String? = null, after: String? = null): Pair<List<Message>, Boolean> {
        val suffix = before?.let { "?before=$it" } ?: after?.let { "?after=$it" } ?: ""
        val result = App.api.request("GET", "/chats/$chatId/messages$suffix")
        val messages = result.getJSONArray("messages")
        return (0 until messages.length()).map { parseMessage(messages.getJSONObject(it)) } to result.getBoolean("hasMore")
    }

    suspend fun chats(): List<Chat> {
        val values = App.api.request("GET", "/chats").getJSONArray("chats")
        return (0 until values.length()).map {
            val value = values.getJSONObject(it)
            Chat(value.getString("id"), value.getString("lastMessage"), value.getLong("lastMessageTimestamp"), UserRepository.parseContact(value.getJSONObject("contact")))
        }
    }

    private fun parseMessage(value: JSONObject) = Message(value.getString("senderId"), value.getString("messageText"), value.getLong("timestamp"), value.getString("id"))
}
