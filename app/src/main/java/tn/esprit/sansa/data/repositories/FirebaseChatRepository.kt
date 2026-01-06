package tn.esprit.sansa.data.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tn.esprit.sansa.ui.screens.models.ChatMessage

class FirebaseChatRepository {
    private val database = FirebaseDatabase.getInstance()
    private val chatsRef = database.getReference("chats")

    /**
     * Listen to real-time messages for a specific room
     */
    fun getMessages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        val query = chatsRef.child(roomId).limitToLast(50)
        query.addValueEventListener(listener)
        awaitClose { query.removeEventListener(listener) }
    }

    /**
     * Send a message to Firebase
     */
    fun sendMessage(roomId: String, message: ChatMessage, onComplete: (Boolean) -> Unit) {
        val id = chatsRef.child(roomId).push().key ?: return
        val finalMessage = message.copy(id = id)
        chatsRef.child(roomId).child(id).setValue(finalMessage)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }
}
