package com.example.lostandfound.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfound.models.ChatRoom
import com.example.lostandfound.models.Post
import com.example.lostandfound.viewmodels.base64ToBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChat: (String, String) -> Unit
) {
    var post by remember { mutableStateOf<Post?>(null) }
    var showResolveDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    LaunchedEffect(postId) {
        val doc = firestore.collection("posts").document(postId).get().await()
        post = doc.toObject(Post::class.java)?.copy(id = doc.id)
    }

    suspend fun createOrOpenChat() {
        val postOwnerId = post?.userId ?: return

        // Check existing chat
        val chatsSnapshot = firestore.collection("chatRooms")
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()

        val existingChat = chatsSnapshot.documents.mapNotNull { doc ->
            doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
        }.firstOrNull { it.participantIds.contains(postOwnerId) }

        if (existingChat != null) {
            val otherUserName = existingChat.participantNames[postOwnerId] ?: "User"
            onNavigateToChat(existingChat.id, otherUserName)
        } else {
            // Create new chat
            val currentUserDoc = firestore.collection("users").document(currentUserId).get().await()
            val currentUserName = currentUserDoc.getString("displayName") ?: "User"

            val postOwnerDoc = firestore.collection("users").document(postOwnerId).get().await()
            val postOwnerName = postOwnerDoc.getString("displayName") ?: "User"

            val chatRoom = ChatRoom(
                participantIds = listOf(currentUserId, postOwnerId),
                participantNames = mapOf(
                    currentUserId to currentUserName,
                    postOwnerId to postOwnerName
                ),
                lastMessage = "",
                lastMessageTime = System.currentTimeMillis(),
                postId = postId
            )

            val docRef = firestore.collection("chatRooms").add(chatRoom).await()
            onNavigateToChat(docRef.id, postOwnerName)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        post?.let { currentPost ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image
                if (currentPost.imageBase64.isNotEmpty()) {
                    base64ToBitmap(currentPost.imageBase64)?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = currentPost.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Content
                Column(modifier = Modifier.padding(16.dp)) {
                    // Type badges
                    Row {
                        Surface(
                            color = if (currentPost.type == "LOST") Color(0xFFEF4444) else Color(0xFF10B981),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = currentPost.type,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        if (currentPost.status == "RESOLVED") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color(0xFF10B981),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "RESOLVED",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentPost.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentPost.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Category: ${currentPost.category}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = currentPost.location)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Contact: ${currentPost.contactInfo}")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Posted by: ${currentPost.userName}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons
                    val isOwner = currentPost.userId == currentUserId

                    if (!isOwner && currentPost.status == "ACTIVE") {
                        Button(
                            onClick = { scope.launch { createOrOpenChat() } },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Contact Owner")
                        }
                    }

                    if (isOwner && currentPost.status == "ACTIVE") {
                        Button(
                            onClick = { showResolveDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text("Mark as Resolved")
                        }
                    }
                }
            }
        }
    }

    // Resolve dialog
    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = { Text("Mark as Resolved") },
            text = { Text("Are you sure this item has been found/returned?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        firestore.collection("posts").document(postId)
                            .update("status", "RESOLVED")
                            .await()
                        showResolveDialog = false
                        onNavigateBack()
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}