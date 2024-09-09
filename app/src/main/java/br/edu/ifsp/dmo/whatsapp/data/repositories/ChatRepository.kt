package br.edu.ifsp.dmo.whatsapp.data.repositories

import br.edu.ifsp.dmo.whatsapp.data.model.Chat
import br.edu.ifsp.dmo.whatsapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatsCollection = firestore.collection("chats")

    suspend fun createChat(participants: List<String>): String? {
        val chatData = hashMapOf(
            "participants" to participants.associateWith { true },
            "lastMessage" to "",
            "lastMessageTimestamp" to System.currentTimeMillis()
        )

        return try {
            val chatRef = chatsCollection.add(chatData).await()
            chatRef.id
        } catch (e: Exception) {
            handleFailure("ChatRepository", e)
            null
        }
    }

    suspend fun sendMessage(chatId: String, message: Message): Boolean {
        val messageData = hashMapOf(
            "senderId" to message.senderId,
            "messageText" to message.messageText,
            "timestamp" to message.timestamp
        )

        return try {
            chatsCollection
                .document(chatId)
                .collection("messages")
                .add(messageData).await()

            chatsCollection
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to message.messageText,
                        "lastMessageTimestamp" to message.timestamp
                    )
                ).await()
            true
        } catch (e: Exception) {
            handleFailure("ChatRepository", e)
            false
        }
    }

    fun getMessagesCollection(chatId: String): CollectionReference {
        return chatsCollection.document(chatId).collection("messages")
    }

    private fun handleFailure(tag: String, e: Exception) {
        println("Erro no $tag: ${e.message}")
    }

    suspend fun getChatsForUser(userId: String): List<Chat> {
        return try {
            val querySnapshot = chatsCollection
                .whereArrayContains("participants", userId)
                .get()
                .await()

            querySnapshot.documents.map { document ->
                val chatId = document.id
                val lastMessage = document.getString("lastMessage") ?: ""
                val lastMessageTimestamp = document.getLong("lastMessageTimestamp") ?: 0
                Chat(chatId, lastMessage, lastMessageTimestamp)
            }
        } catch (e: Exception) {
            handleFailure("ChatRepository", e)
            emptyList()
        }
    }

    suspend fun getOrCreateChat(participants: List<String>): String? {
        return try {
            // Procura chats que contêm todos os participantes
            val querySnapshot = chatsCollection
                .whereEqualTo("participants", participants.associateWith { true })
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                // Retorna o ID do primeiro chat encontrado
                querySnapshot.documents.first().id
            } else {
                // Se não encontrou, cria um novo chat
                createChat(participants)
            }
        } catch (e: Exception) {
            handleFailure("ChatRepository", e)
            null
        }
    }
}
