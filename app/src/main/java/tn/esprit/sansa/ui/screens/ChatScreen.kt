package tn.esprit.sansa.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import tn.esprit.sansa.ui.screens.models.ChatMessage
import tn.esprit.sansa.ui.viewmodels.ChatViewModel
import tn.esprit.sansa.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: String,
    currentUserName: String,
    targetUserId: String,
    targetUserName: String,
    viewModel: ChatViewModel = viewModel(),
    onBackPressed: () -> Unit = {}
) {
    val messages by viewModel.messages.collectAsState()
    val smartReplies by viewModel.smartReplies.collectAsState()
    var textMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Configuration de la room privée
    LaunchedEffect(targetUserId) {
        android.util.Log.d("ChatScreen", "Current User ID: $currentUserId, Target User ID: $targetUserId")
        viewModel.startChat(currentUserId, targetUserId)
    }

    // Auto-scroll au dernier message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(targetUserName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("En ligne", fontSize = 11.sp, color = NoorGreen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(Color.White)) {
                // Smart Replies Bar
                if (smartReplies.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        smartReplies.forEach { reply ->
                            SuggestionChip(
                                onClick = { viewModel.sendMessage(reply, currentUserId, currentUserName) },
                                label = { Text(reply) },
                                shape = RoundedCornerShape(16.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = NoorBlue.copy(alpha = 0.1f),
                                    labelColor = NoorBlue
                                )
                            )
                        }
                    }
                }

                // Input Bar
                Surface(tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = textMessage,
                            onValueChange = { textMessage = it },
                            placeholder = { Text("Tapez un message...") },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                if (textMessage.isNotBlank()) {
                                    viewModel.sendMessage(textMessage, currentUserId, currentUserName)
                                    textMessage = ""
                                }
                            },
                            containerColor = NoorBlue,
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F7FB)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message,
                    isFromMe = message.senderId == currentUserId
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, isFromMe: Boolean) {
    android.util.Log.d("ChatBubble", "Message from: ${message.senderId}, isFromMe: $isFromMe, text: ${message.text}")
    
    val alignment = if (isFromMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isFromMe) NoorBlue else Color.White
    val contentColor = if (isFromMe) Color.White else Color.Black
    val shape = if (isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = shape,
            tonalElevation = if (isFromMe) 0.dp else 4.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.text, color = contentColor, fontSize = 15.sp)
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    color = contentColor.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
