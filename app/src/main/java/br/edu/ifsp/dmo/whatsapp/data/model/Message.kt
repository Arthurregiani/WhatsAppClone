package br.edu.ifsp.dmo.whatsapp.data.model

data class Message(
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
