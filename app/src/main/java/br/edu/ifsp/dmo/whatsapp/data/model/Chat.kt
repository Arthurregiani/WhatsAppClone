package br.edu.ifsp.dmo.whatsapp.data.model

data class Chat(
    val id: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long
)