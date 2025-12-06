package com.example.lostandfound.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.models.Post
import com.example.lostandfound.viewmodels.PostViewModel
import com.example.lostandfound.viewmodels.base64ToBitmap
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
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
    val context = LocalContext.current

    var userName by remember { mutableStateOf("User") }
    var userEmail by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditPostDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    var editNameText by remember { mutableStateOf("") }
    var editPostTitle by remember { mutableStateOf("") }
    var editPostDescription by remember { mutableStateOf("") }
    var editPostImageUri by remember { mutableStateOf<Uri?>(null) }
    var editPostImageBase64 by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf<String?>(null) }
    var directPostCount by remember { mutableIntStateOf(0) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Image picker for editing
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editPostImageUri = it
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                editPostImageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error converting image: ${e.message}")
                updateMessage = "Failed to load image"
            }
        }
    }

    // Load user info and posts
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "🚀 ProfileScreen LaunchedEffect triggered")

        currentUserId?.let { userId ->
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                userName = doc.getString("displayName") ?: "User"
                userEmail = doc.getString("email") ?: ""
                Log.d("ProfileScreen", "✅ User info loaded: $userName")
            } catch (e: Exception) {
                Log.e("ProfileScreen", "❌ Error loading user info: ${e.message}")
            }

            try {
                val snapshot = firestore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                directPostCount = snapshot.documents.size
            } catch (e: Exception) {
                Log.e("ProfileScreen", "❌ Direct query failed: ${e.message}")
            }

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
                Snackbar(modifier = Modifier.padding(16.dp)) {
                    Text(message)
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { scope.launch { viewModel.loadMyPosts() } },
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
                        Text(
                            text = "${posts.size} Posts",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
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
                Text(
                    text = "My Posts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                // Posts List
                if (posts.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No posts yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        items(posts, key = { it.id }) { post ->
                            ProfilePostItem(
                                post = post,
                                onClick = { onNavigateToPostDetail(post.id) },
                                onEdit = {
                                    postToEdit = post
                                    editPostTitle = post.title
                                    editPostDescription = post.description
                                    editPostImageBase64 = post.imageBase64
                                    editPostImageUri = null
                                    showEditPostDialog = true
                                },
                                onDelete = {
                                    postToDelete = post
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Post Dialog
    if (showEditPostDialog && postToEdit != null) {
        AlertDialog(
            onDismissRequest = { if (!isUpdating) showEditPostDialog = false },
            icon = { Icon(Icons.Default.Edit, "Edit Post") },
            title = { Text("Edit Post") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    OutlinedTextField(
                        value = editPostTitle,
                        onValueChange = { if (it.length <= 100) editPostTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        enabled = !isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("${editPostTitle.length}/100") }
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editPostDescription,
                        onValueChange = { if (it.length <= 500) editPostDescription = it },
                        label = { Text("Description") },
                        minLines = 3,
                        maxLines = 5,
                        enabled = !isUpdating,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("${editPostDescription.length}/500") }
                    )

                    Spacer(Modifier.height(12.dp))

                    // Image Preview
                    if (editPostImageUri != null || editPostImageBase64.isNotEmpty()) {
                        val bitmap = if (editPostImageUri != null) {
                            base64ToBitmap(editPostImageBase64)
                        } else {
                            base64ToBitmap(editPostImageBase64)
                        }

                        bitmap?.let {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = "Post Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            enabled = !isUpdating,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Change Image")
                        }

                        if (editPostImageBase64.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    editPostImageBase64 = ""
                                    editPostImageUri = null
                                },
                                enabled = !isUpdating,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }

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
                            val title = editPostTitle.trim()
                            val description = editPostDescription.trim()

                            when {
                                title.isEmpty() -> updateMessage = "Title cannot be empty"
                                description.isEmpty() -> updateMessage = "Description cannot be empty"
                                else -> {
                                    isUpdating = true
                                    try {
                                        postToEdit?.let { post ->
                                            val updates = hashMapOf<String, Any>(
                                                "title" to title,
                                                "description" to description,
                                                "imageBase64" to editPostImageBase64
                                            )

                                            firestore.collection("posts")
                                                .document(post.id)
                                                .update(updates)
                                                .await()

                                            updateMessage = "✅ Post updated successfully"
                                            showEditPostDialog = false
                                            postToEdit = null

                                            // Reload posts
                                            viewModel.loadMyPosts()

                                            Log.d("ProfileScreen", "✅ Post updated: ${post.id}")
                                        }
                                    } catch (e: Exception) {
                                        updateMessage = "❌ Failed to update: ${e.message}"
                                        Log.e("ProfileScreen", "❌ Update error: ${e.message}")
                                    } finally {
                                        isUpdating = false
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isUpdating && editPostTitle.trim().isNotEmpty() && editPostDescription.trim().isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditPostDialog = false
                        postToEdit = null
                    },
                    enabled = !isUpdating
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (!isUpdating) {
                    showDeleteDialog = false
                    postToDelete = null
                }
            },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    "Delete Post",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Post?") },
            text = {
                Column {
                    Text("Are you sure you want to delete this post?")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "\"${postToDelete?.title}\"",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "This action cannot be undone.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
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
                            isUpdating = true
                            try {
                                postToDelete?.let { post ->
                                    firestore.collection("posts")
                                        .document(post.id)
                                        .delete()
                                        .await()

                                    updateMessage = "✅ Post deleted successfully"
                                    showDeleteDialog = false
                                    postToDelete = null

                                    // Reload posts
                                    viewModel.loadMyPosts()

                                    Log.d("ProfileScreen", "✅ Post deleted: ${post.id}")
                                }
                            } catch (e: Exception) {
                                updateMessage = "❌ Failed to delete: ${e.message}"
                                Log.e("ProfileScreen", "❌ Delete error: ${e.message}")
                            } finally {
                                isUpdating = false
                            }
                        }
                    },
                    enabled = !isUpdating,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        postToDelete = null
                    },
                    enabled = !isUpdating
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { if (!isUpdating) showEditNameDialog = false },
            icon = { Icon(Icons.Default.Edit, "Edit Name") },
            title = { Text("Edit Display Name") },
            text = {
                Column {
                    Text("Enter your new display name:")
                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editNameText,
                        onValueChange = { if (it.length <= 30) editNameText = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        enabled = !isUpdating,
                        supportingText = { Text("${editNameText.length}/30 characters") }
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
                            when {
                                newName.isEmpty() -> updateMessage = "Name cannot be empty"
                                newName.length < 2 -> updateMessage = "Name must be at least 2 characters"
                                newName == userName -> showEditNameDialog = false
                                else -> {
                                    isUpdating = true
                                    try {
                                        currentUserId?.let { userId ->
                                            firestore.collection("users")
                                                .document(userId)
                                                .update("displayName", newName)
                                                .await()

                                            userName = newName
                                            updateMessage = "✅ Name updated successfully!"
                                            showEditNameDialog = false
                                        }
                                    } catch (e: Exception) {
                                        updateMessage = "❌ Failed to update: ${e.message}"
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

@Composable
fun ProfilePostItem(
    post: Post,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header with Edit and Delete Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (post.type == "LOST") Color(0xFFEF4444) else Color(0xFF10B981),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = post.type,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    if (post.status == "RESOLVED") {
                        Surface(
                            color = Color(0xFF10B981),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "RESOLVED",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Post",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Post",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Clickable content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                // Image
                if (post.imageBase64.isNotEmpty()) {
                    val bitmap = base64ToBitmap(post.imageBase64)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = post.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Content
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = post.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = post.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}