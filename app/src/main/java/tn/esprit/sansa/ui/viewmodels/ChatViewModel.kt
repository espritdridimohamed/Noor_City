package tn.esprit.sansa.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tn.esprit.sansa.data.repositories.FirebaseChatRepository
import tn.esprit.sansa.ui.screens.models.*

class ChatViewModel(
    private val repository: FirebaseChatRepository = FirebaseChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies: StateFlow<List<String>> = _smartReplies.asStateFlow()

    private val _users = MutableStateFlow<List<UserAccount>>(emptyList())
    val users: StateFlow<List<UserAccount>> = _users.asStateFlow()

    private var currentRoomId: String = ""

    /**
     * Get unique room ID for two users (sorted alphabetically)
     */
    fun startChat(myId: String, targetId: String) {
        currentRoomId = if (myId < targetId) "${myId}_${targetId}" else "${targetId}_${myId}"
        android.util.Log.d("ChatViewModel", "Starting chat - myId: $myId, targetId: $targetId, roomId: $currentRoomId")
        observeMessages(currentRoomId)
    }

    private fun observeMessages(roomId: String) {
        viewModelScope.launch {
            android.util.Log.d("ChatViewModel", "Observing messages for room: $roomId")
            repository.getMessages(roomId).collect { msgList ->
                android.util.Log.d("ChatViewModel", "Received ${msgList.size} messages")
                _messages.value = msgList
                updateSmartReplies(msgList.lastOrNull())
            }
        }
    }

    fun fetchEligibleUsers(myId: String) {
        viewModelScope.launch {
            try {
                val userList = mutableListOf<UserAccount>()
                
                // Add backdoor accounts if not current user
                if (myId != "ADMIN_BACKDOOR") {
                    userList.add(UserAccount(
                        uid = "ADMIN_BACKDOOR",
                        name = "Administrateur Système",
                        email = "admin@noorcity.tn",
                        role = UserRole.ADMIN
                    ))
                }
                if (myId != "TECH001") {
                    userList.add(UserAccount(
                        uid = "TECH001",
                        name = "Ahmed Ben Salem",
                        email = "ahmed.bensalem@sansa.tn",
                        role = UserRole.TECHNICIAN,
                        specialty = "Électricité",
                        workingZone = "Zone A",
                        isVerified = true,
                        isFirstLogin = false
                    ))
                }
                
                // Get all admins from /users
                val usersSnapshot = FirebaseDatabase.getInstance().getReference("users").get().await()
                val admins = usersSnapshot.children.mapNotNull { it.getValue(UserAccount::class.java) }
                    .filter { it.uid != myId && it.role == UserRole.ADMIN }
                userList.addAll(admins)
                
                // Get only activated technicians from /technicians
                val techSnapshot = FirebaseDatabase.getInstance().getReference("technicians").get().await()
                val technicians = techSnapshot.children.mapNotNull { it.getValue(UserAccount::class.java) }
                    .filter { it.uid != myId }
                userList.addAll(technicians)
                
                _users.value = userList
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun sendMessage(text: String, senderId: String, senderName: String) {
        if (text.isBlank() || currentRoomId.isEmpty()) return
        
        val message = ChatMessage(
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT
        )
        
        android.util.Log.d("ChatViewModel", "Sending message to room: $currentRoomId, from: $senderName")
        repository.sendMessage(currentRoomId, message) { success ->
            android.util.Log.d("ChatViewModel", "Message sent: $success")
        }
    }

    private fun updateSmartReplies(lastMessage: ChatMessage?) {
        if (lastMessage == null) {
            _smartReplies.value = emptyList()
            return
        }

        val text = lastMessage.text.lowercase()
        val suggestions = when {
            text.contains("ça va") || text.contains("bonjour") -> listOf("Bonjour !", "Ça va bien", "Salut")
            text.contains("lampadaire") -> listOf("Je m'en occupe", "C'est fait", "Où ?")
            text.contains("panne") -> listOf("J'arrive", "Envoyez l'adresse", "Besoin d'aide ?")
            else -> listOf("Ok", "Entendu", "Merci")
        }
        _smartReplies.value = suggestions
    }
}
