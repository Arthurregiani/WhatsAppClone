package br.edu.ifsp.dmo.whatsapp.data.repositories

import br.edu.ifsp.dmo.whatsapp.data.model.Chat
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {

    // Instância do Firestore e referência para a coleção de chats
    private val firestore = FirebaseFirestore.getInstance()
    private val chatsCollection = firestore.collection("chats")

    // Cria um novo chat com os participantes fornecidos
    suspend fun createChat(participants: List<String>): String? {
        val chatData = mapOf(
            "participants" to participants.associateWith { true },
            "lastMessage" to "",
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        return try {
            val chatRef = chatsCollection.add(chatData).await()
            chatRef.id
        } catch (e: Exception) {
            logError("ChatRepository", e)
            null
        }
    }

    // Envia uma mensagem para um chat específico
    suspend fun sendMessage(chatId: String, message: Message): Boolean {
        return try {
            val messageData = mapOf(
                "senderId" to message.senderId,
                "messageText" to message.messageText,
                "timestamp" to message.timestamp
            )

            // Adiciona a mensagem na coleção de mensagens
            chatsCollection
                .document(chatId)
                .collection("messages")
                .add(messageData).await()

            // Atualiza o último envio de mensagem
            updateLastMessage(chatId, message.messageText, message.timestamp)
            true
        } catch (e: Exception) {
            logError("ChatRepository", e)
            false
        }
    }

    // Obtém a coleção de mensagens para um chat específico
    fun getMessagesCollection(chatId: String): CollectionReference {
        return chatsCollection.document(chatId).collection("messages")
    }

    // Atualiza o último envio de mensagem em um chat
    private suspend fun updateLastMessage(chatId: String, lastMessage: String, timestamp: Long) {
        try {
            chatsCollection
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to lastMessage,
                        "lastMessageTimestamp" to timestamp
                    )
                ).await()
        } catch (e: Exception) {
            logError("ChatRepository", e)
        }
    }

    // Obtém ou cria um chat para os participantes fornecidos
    suspend fun getOrCreateChat(participants: List<String>): String? {
        return try {
            val querySnapshot = chatsCollection
                .whereEqualTo("participants", participants.associateWith { true })
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.first().id
            } else {
                createChat(participants)
            }
        } catch (e: Exception) {
            logError("ChatRepository", e)
            null
        }
    }

    // Registra erros no console
    private fun logError(tag: String, e: Exception) {
        println("Erro no $tag: ${e.message}")
    }

    // Método comentado: Obtém chats para um usuário específico
    /*
    suspend fun getChatsForUser(userId: String): List<Chat> {
        return try {
            val querySnapshot = chatsCollection
                .whereArrayContains("participants", userId)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                val chatId = document.id
                val lastMessage = document.getString("lastMessage") ?: ""
                val lastMessageTimestamp = document.getLong("lastMessageTimestamp") ?: 0L
                Chat(chatId, lastMessage, lastMessageTimestamp)
            }
        } catch (e: Exception) {
            logError("ChatRepository", e)
            emptyList()
        }
    }
    */
}
