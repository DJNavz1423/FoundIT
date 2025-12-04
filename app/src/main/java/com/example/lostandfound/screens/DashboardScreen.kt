package com.example.lostandfound.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.models.Post
import com.example.lostandfound.viewmodels.PostViewModel
import com.example.lostandfound.viewmodels.base64ToBitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PostFilter {
    ALL, LOST, FOUND
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: PostViewModel = viewModel(key = "dashboard")
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf(PostFilter.ALL) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Direct Firebase check for debugging
    var directCount by remember { mutableStateOf(0) }

    // Filter posts based on selection
    val filteredPosts = remember(posts, selectedFilter) {
        when (selectedFilter) {
            PostFilter.ALL -> posts
            PostFilter.LOST -> posts.filter { it.type == "LOST" }
            PostFilter.FOUND -> posts.filter { it.type == "FOUND" }
        }
    }

    // Debug: Print everything
    LaunchedEffect(posts) {
        Log.d("DashboardScreen", "=== POSTS STATE CHANGED ===")
        Log.d("DashboardScreen", "Posts count: ${posts.size}")
        Log.d("DashboardScreen", "Filtered count: ${filteredPosts.size}")
        Log.d("DashboardScreen", "Current filter: $selectedFilter")
    }

    // Load posts when screen appears
    LaunchedEffect(Unit) {
        Log.d("DashboardScreen", "🚀 DashboardScreen LaunchedEffect triggered")

        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("DashboardScreen", "Current user: ${currentUser?.uid ?: "NOT LOGGED IN"}")

        // Direct Firebase query
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { snapshot ->
                directCount = snapshot.documents.size
                Log.d("DashboardScreen", "📊 DIRECT Firebase: ${snapshot.documents.size} posts")
            }

        viewModel.loadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("FoundIT")
                        Text(
                            "Posts: ${filteredPosts.size}/${posts.size}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterMenu = true }) {
                        BadgedBox(
                            badge = {
                                if (selectedFilter != PostFilter.ALL) {
                                    Badge(
                                        containerColor = Color(0xFF10B981),
                                        contentColor = Color.White
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, "Filter", tint = Color.White)
                        }
                    }

                    // Refresh button
                    IconButton(onClick = {
                        Log.d("DashboardScreen", "🔄 Manual refresh button clicked")
                        scope.launch { viewModel.loadPosts() }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = {
                    Log.d("DashboardScreen", "🔄 Pull to refresh triggered")
                    scope.launch { viewModel.loadPosts() }
                },
                state = pullToRefreshState
            ) {
                Column {
                    // Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == PostFilter.ALL,
                            onClick = { selectedFilter = PostFilter.ALL },
                            label = { Text("All (${posts.size})") },
                            leadingIcon = if (selectedFilter == PostFilter.ALL) {
                                { Icon(Icons.Default.FilterList, null, Modifier.size(18.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = selectedFilter == PostFilter.LOST,
                            onClick = { selectedFilter = PostFilter.LOST },
                            label = {
                                Text("Lost (${posts.count { it.type == "LOST" }})")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEF4444),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedFilter == PostFilter.FOUND,
                            onClick = { selectedFilter = PostFilter.FOUND },
                            label = {
                                Text("Found (${posts.count { it.type == "FOUND" }})")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    // Posts List
                    if (filteredPosts.isEmpty() && !isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = when (selectedFilter) {
                                        PostFilter.ALL -> "No posts yet\nTap + to create one"
                                        PostFilter.LOST -> "No lost items posted"
                                        PostFilter.FOUND -> "No found items posted"
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (isLoading && posts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(filteredPosts, key = { it.id }) { post ->
                                PostItem(post, onClick = { onNavigateToPostDetail(post.id) })
                            }
                        }
                    }
                }
            }

            // Filter Menu Dialog
            if (showFilterMenu) {
                AlertDialog(
                    onDismissRequest = { showFilterMenu = false },
                    icon = { Icon(Icons.Default.FilterList, "Filter") },
                    title = { Text("Filter Posts") },
                    text = {
                        Column {
                            Text("Show posts by type:", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(16.dp))

                            FilterMenuItem(
                                text = "All Posts (${posts.size})",
                                selected = selectedFilter == PostFilter.ALL,
                                onClick = {
                                    selectedFilter = PostFilter.ALL
                                    showFilterMenu = false
                                }
                            )

                            FilterMenuItem(
                                text = "Lost Items (${posts.count { it.type == "LOST" }})",
                                selected = selectedFilter == PostFilter.LOST,
                                color = Color(0xFFEF4444),
                                onClick = {
                                    selectedFilter = PostFilter.LOST
                                    showFilterMenu = false
                                }
                            )

                            FilterMenuItem(
                                text = "Found Items (${posts.count { it.type == "FOUND" }})",
                                selected = selectedFilter == PostFilter.FOUND,
                                color = Color(0xFF10B981),
                                onClick = {
                                    selectedFilter = PostFilter.FOUND
                                    showFilterMenu = false
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showFilterMenu = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun FilterMenuItem(
    text: String,
    selected: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface
            )

            if (selected) {
                Surface(
                    color = color,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "✓",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = post.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Status: ${post.status}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
            }

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
                    maxLines = 3,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.location,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = post.category,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimestamp(post.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}