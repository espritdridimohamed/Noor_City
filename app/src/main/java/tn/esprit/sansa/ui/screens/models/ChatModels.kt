package tn.esprit.sansa.ui.screens.models

enum class MessageType {
    TEXT, IMAGE, SYSTEM
}

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val type: MessageType = MessageType.TEXT
)

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L,
    val participantIds: List<String> = emptyList()
)
