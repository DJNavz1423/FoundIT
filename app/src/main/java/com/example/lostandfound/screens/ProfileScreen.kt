package com.example.lostandfound.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.viewmodels.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToPostDetail: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: PostViewModel = viewModel(key = "profile")
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var directPostCount by remember { mutableIntStateOf(0) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Load user info and posts
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "🚀 ProfileScreen LaunchedEffect triggered")
        Log.d("ProfileScreen", "Current user ID: $currentUserId")

        currentUserId?.let { userId ->
            // Load user info
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                userName = doc.getString("displayName") ?: "User"
                userEmail = doc.getString("email") ?: ""
                Log.d("ProfileScreen", "✅ User info loaded: $userName ($userEmail)")
            } catch (e: Exception) {
                Log.e("ProfileScreen", "❌ Error loading user info: ${e.message}")
            }

            // Direct Firebase check
            try {
                val snapshot = firestore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                directPostCount = snapshot.documents.size
                Log.d("ProfileScreen", "📊 DIRECT Firebase: ${snapshot.documents.size} posts")
            } catch (e: Exception) {
                Log.e("ProfileScreen", "❌ Direct query failed: ${e.message}")
            }

            // Load through ViewModel
            viewModel.loadMyPosts()
        }
    }

    // Clear message after showing
    LaunchedEffect(updateMessage) {
        updateMessage?.let {
            kotlinx.coroutines.delay(3000)
            updateMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = {
                        Log.d("ProfileScreen", "🔄 Manual refresh clicked")
                        scope.launch { viewModel.loadMyPosts() }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
                    }
                }
            )
        },
        snackbarHost = {
            updateMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(message)
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = {
                Log.d("ProfileScreen", "🔄 Pull to refresh triggered")
                scope.launch { viewModel.loadMyPosts() }
            },
            state = pullToRefreshState,
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Profile Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Name with Edit Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userName,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    editNameText = userName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userEmail,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Text(
                                text = "${posts.size} Posts",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " (Direct: $directPostCount)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showLogoutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Logout")
                        }
                    }
                }

                // Posts Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Posts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                }

                // Posts List or Empty State
                if (posts.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "No posts yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            PostItem(post, onClick = { onNavigateToPostDetail(post.id) })
                        }
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isUpdating) showEditNameDialog = false
            },
            icon = { Icon(Icons.Default.Edit, "Edit Name") },
            title = { Text("Edit Display Name") },
            text = {
                Column {
                    Text(
                        "Enter your new display name:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = {
                            if (it.length <= 30) editNameText = it
                        },
                        label = { Text("Display Name") },
                        singleLine = true,
                        enabled = !isUpdating,
                        supportingText = {
                            Text("${editNameText.length}/30 characters")
                        }
                    )

                    if (isUpdating) {
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val newName = editNameText.trim()

                            // Validation
                            when {
                                newName.isEmpty() -> {
                                    updateMessage = "Name cannot be empty"
                                }
                                newName.length < 2 -> {
                                    updateMessage = "Name must be at least 2 characters"
                                }
                                newName == userName -> {
                                    showEditNameDialog = false
                                }
                                else -> {
                                    // Update in Firestore
                                    isUpdating = true
                                    try {
                                        currentUserId?.let { userId ->
                                            firestore.collection("users")
                                                .document(userId)
                                                .update("displayName", newName)
                                                .await()

                                            // Update local state
                                            userName = newName
                                            updateMessage = "✅ Name updated successfully!"
                                            showEditNameDialog = false

                                            Log.d("ProfileScreen", "✅ Display name updated to: $newName")
                                        }
                                    } catch (e: Exception) {
                                        updateMessage = "❌ Failed to update name: ${e.message}"
                                        Log.e("ProfileScreen", "❌ Error updating name: ${e.message}")
                                    } finally {
                                        isUpdating = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isUpdating && editNameText.trim().isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false },
                    enabled = !isUpdating
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}