package com.example.lostandfound.screens

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.models.Message
import com.example.lostandfound.utils.NotificationHelper
import com.example.lostandfound.viewmodels.UnreadMessagesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoomId: String,
    otherUserName: String,
    onNavigateBack: () -> Unit,
    unreadViewModel: UnreadMessagesViewModel = viewModel()
) {
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var otherUserId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    // Mark chat as read when opening
    LaunchedEffect(chatRoomId) {
        unreadViewModel.markChatAsRead(chatRoomId)

        // Get other user ID
        val chatDoc = firestore.collection("chatRooms").document(chatRoomId).get().await()
        val participantIds = chatDoc.get("participantIds") as? List<*>
        otherUserId = participantIds?.firstOrNull { it != currentUserId } as? String

        // Listen for messages
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                scope.launch {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }
    }

    suspend fun sendMessage() {
        if (messageText.isBlank() || otherUserId == null) return

        val userDoc = firestore.collection("users").document(currentUserId).get().await()
        val userName = userDoc.getString("displayName") ?: "User"

        val message = Message(
            senderId = currentUserId,
            senderName = userName,
            text = messageText,
            timestamp = System.currentTimeMillis()
        )

        // Save message
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .add(message)
            .await()

        // Update chat room
        firestore.collection("chatRooms")
            .document(chatRoomId)
            .update(
                mapOf(
                    "lastMessage" to messageText,
                    "lastMessageTime" to message.timestamp
                )
            )

        // Increment unread count for other user
        otherUserId?.let { recipientId ->
            unreadViewModel.incrementUnreadCount(chatRoomId, recipientId)

            // Send notification
            try {
                NotificationHelper.sendMessageNotification(
                    context = context,
                    recipientUserId = recipientId,
                    senderName = userName,
                    messageText = messageText,
                    chatRoomId = chatRoomId
                )
            } catch (e: Exception) {
                Log.e("ChatScreen", "Error sending notification: ${e.message}")
            }
        }

        messageText = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(otherUserName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message, currentUserId)
                }
            }

            // Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        scope.launch { sendMessage() }
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, currentUserId: String) {
    val isSent = message.senderId == currentUserId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isSent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .padding(horizontal = if (isSent) 48.dp else 0.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isSent) {
                    Text(
                        text = message.senderName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 15.sp,
                    color = if (isSent) Color.White else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                    fontSize = 11.sp,
                    color = if (isSent) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}