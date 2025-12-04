package com.example.lostandfound.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lostandfound.viewmodels.UnreadMessagesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit,
    onLogout: () -> Unit,
    unreadViewModel: UnreadMessagesViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val unreadCount by unreadViewModel.totalUnreadCount.collectAsState()
    val hasUnread by unreadViewModel.hasUnreadMessages.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Dashboard") },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                    }}
                )

                // Chats with Badge
                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (hasUnread) {
                                    Badge(
                                        containerColor = Color.Red,
                                        contentColor = Color.White
                                    ) {
                                        if (unreadCount > 0) {
                                            Text(
                                                text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Email, "Chats")
                        }
                    },
                    label = { Text("Chats") },
                    selected = currentRoute == "chats",
                    onClick = { navController.navigate("chats") {
                        popUpTo("dashboard")
                    }}
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") },
                    selected = currentRoute == "profile",
                    onClick = { navController.navigate("profile") {
                        popUpTo("dashboard")
                    }}
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == "dashboard") {
                FloatingActionButton(onClick = onNavigateToCreatePost) {
                    Icon(Icons.Default.Add, "Create Post")
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") {
                DashboardScreen(onNavigateToPostDetail)
            }
            composable("chats") {
                ChatsScreen(onNavigateToChat, unreadViewModel)
            }
            composable("profile") {
                ProfileScreen(onNavigateToPostDetail, onLogout)
            }
        }
    }
}