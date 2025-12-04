package com.example.lostandfound.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.models.ChatRoom
import com.example.lostandfound.viewmodels.UnreadMessagesViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onNavigateToChat: (String, String) -> Unit,
    unreadViewModel: UnreadMessagesViewModel = viewModel()
) {
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    fun loadChats() {
        isLoading = true
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("ChatsScreen", "User not logged in")
            isLoading = false
            return
        }

        Log.d("ChatsScreen", "Loading chats for user: $userId")

        firestore.collection("chatRooms")
            .whereArrayContains("participantIds", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatsScreen", "Error loading chats: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }

                chatRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                Log.d("ChatsScreen", "Loaded ${chatRooms.size} chat rooms")
                isLoading = false
            }
    }

    LaunchedEffect(Unit) {
        loadChats()
    }

    fun getOtherUserName(chatRoom: ChatRoom): String {
        val currentUserId = auth.currentUser?.uid ?: return ""
        return chatRoom.participantNames.entries
            .firstOrNull { it.key != currentUserId }?.value ?: "User"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                actions = {
                    IconButton(onClick = { loadChats() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { scope.launch { loadChats() } },
            state = pullToRefreshState,
            modifier = Modifier.padding(padding)
        ) {
            if (chatRooms.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No conversations yet\nContact post owners to start chatting",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(chatRooms) { chatRoom ->
                        val unreadCount = chatRoom.unreadCount[currentUserId] ?: 0
                        ChatRoomItem(
                            chatRoom = chatRoom,
                            otherUserName = getOtherUserName(chatRoom),
                            unreadCount = unreadCount,
                            onClick = {
                                onNavigateToChat(chatRoom.id, getOtherUserName(chatRoom))
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    otherUserName: String,
    unreadCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = otherUserName,
                    fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color.Red,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chatRoom.lastMessage.ifEmpty { "No messages yet" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 1,
                fontWeight = if (unreadCount > 0) FontWeight.Medium else FontWeight.Normal
            )
        }

        Text(
            text = formatTimestamp(chatRoom.lastMessageTime),
            fontSize = 12.sp,
            color = if (unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (unreadCount > 0) FontWeight.Bold else FontWeight.Normal
        )
    }
}