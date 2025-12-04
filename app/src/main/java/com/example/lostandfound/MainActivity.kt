package com.example.lostandfound

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lostandfound.screens.*
import com.example.lostandfound.ui.theme.LostAndFoundTheme
import com.example.lostandfound.utils.NotificationHelper
import com.example.lostandfound.viewmodels.AuthState
import com.example.lostandfound.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.initializeFCM()
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // Older versions don’t need permission → Initialize FCM directly
            NotificationHelper.initializeFCM()
        }

        setContent {
            LostAndFoundTheme {
                LostAndFoundApp(authViewModel)
            }
        }
    }
}

@Composable
fun LostAndFoundApp(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Authenticated -> {
                navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // Auth screens
        composable("login") {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate("signup") },
                onLoginSuccess = { navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }},
                viewModel = authViewModel
            )
        }

        composable("signup") {
            SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignUpSuccess = { navController.navigate("main") {
                    popUpTo(0) { inclusive = true }
                }},
                viewModel = authViewModel
            )
        }

        // Main app
        composable("main") {
            MainScreen(
                onNavigateToCreatePost = { navController.navigate("create_post") },
                onNavigateToPostDetail = { postId -> navController.navigate("post_detail/$postId") },
                onNavigateToChat = { chatRoomId, userName ->
                    navController.navigate("chat/$chatRoomId/$userName")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("create_post") {
            CreatePostScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "post_detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { chatRoomId, userName ->
                    navController.navigate("chat/$chatRoomId/$userName")
                }
            )
        }

        composable(
            route = "chat/{chatRoomId}/{userName}",
            arguments = listOf(
                navArgument("chatRoomId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            ChatScreen(
                chatRoomId = chatRoomId,
                otherUserName = userName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}