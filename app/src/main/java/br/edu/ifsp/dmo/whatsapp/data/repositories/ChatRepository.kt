package br.edu.ifsp.dmo.whatsapp.data.repositories

import br.edu.ifsp.dmo.whatsapp.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatsCollection = firestore.collection("chats")

    // Função para criar um novo chat entre os participantes
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

    // Função para enviar uma mensagem em um chat específico
    suspend fun sendMessage(chatId: String, message: Message): Boolean {
        val messageData = hashMapOf(
            "senderId" to message.senderId,
            "messageText" to message.messageText,
            "timestamp" to message.timestamp
        )

        return try {
            // Adiciona a mensagem na subcoleção de mensagens do chat
            chatsCollection
                .document(chatId)
                .collection("messages")
                .add(messageData).await()

            // Atualiza o documento do chat com a última mensagem
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


    // Função para buscar as mensagens de um chat
    suspend fun getMessages(chatId: String): List<Message> {
        return try {
            val messagesSnapshot = chatsCollection
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .get()
                .await()

            messagesSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Message::class.java)
            }
        } catch (e: Exception) {
            handleFailure("ChatRepository", e)
            emptyList()
        }
    }

    // Função para buscar todas as conversas de um usuário
    suspend fun getUserChats(userId: String): List<Map<String, Any>> {
        return try {
            val chatsSnapshot = chatsCollection
                .whereEqualTo("participants.$userId", true)
                .get()
                .await()

            chatsSnapshot.documents.map { doc ->
                doc.data ?: emptyMap<String, Any>()
            }
        } catch (e: Exception) {
            handleFailure("ChatRepository", e)
            emptyList()
        }
    }

    // Função para lidar com falhas
    private fun handleFailure(tag: String, e: Exception) {
        // Aqui você pode implementar um sistema de logs ou tratamento de erros
        println("Erro no $tag: ${e.message}")
    }
}
