package com.example.lostandfound.screens

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lostandfound.viewmodels.AuthState
import com.example.lostandfound.viewmodels.AuthViewModel
import com.example.lostandfound.viewmodels.EmailValidationState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showEmailValidationDialog by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val emailValidationState by viewModel.emailValidationState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onSignUpSuccess()
            is AuthState.Error -> errorMessage = (authState as AuthState.Error).message
            else -> {}
        }
    }

    // Show validation dialog when email validation completes
    LaunchedEffect(emailValidationState) {
        when (emailValidationState) {
            is EmailValidationState.Valid,
            is EmailValidationState.Invalid -> {
                showEmailValidationDialog = true
            }
            else -> {}
        }
    }

    // Email Validation Dialog
    if (showEmailValidationDialog) {
        AlertDialog(
            onDismissRequest = {
                showEmailValidationDialog = false
                viewModel.resetEmailValidation()
            },
            icon = {
                Icon(
                    imageVector = if (emailValidationState is EmailValidationState.Valid)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.Error,
                    contentDescription = null,
                    tint = if (emailValidationState is EmailValidationState.Valid)
                        Color(0xFF4CAF50)
                    else
                        MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (emailValidationState is EmailValidationState.Valid)
                        "Email Verified"
                    else
                        "Email Validation Failed"
                )
            },
            text = {
                Text(
                    text = when (emailValidationState) {
                        is EmailValidationState.Valid ->
                            "Your email has been verified. You can continue with the registration."
                        is EmailValidationState.Invalid ->
                            (emailValidationState as EmailValidationState.Invalid).message
                        else -> ""
                    }
                )
            },
            confirmButton = {
                if (emailValidationState is EmailValidationState.Valid) {
                    TextButton(
                        onClick = {
                            showEmailValidationDialog = false
                            // Proceed with sign up
                            when {
                                name.isEmpty() ->
                                    errorMessage = "Please enter your name"
                                password.isEmpty() ->
                                    errorMessage = "Please enter a password"
                                password != confirmPassword ->
                                    errorMessage = "Passwords don't match"
                                password.length < 6 ->
                                    errorMessage = "Password must be at least 6 characters"
                                else -> {
                                    errorMessage = ""
                                    viewModel.signUp(name, email, password)
                                }
                            }
                        }
                    ) {
                        Text("Continue")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEmailValidationDialog = false
                        viewModel.resetEmailValidation()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Sign up to get started",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = ""
                    viewModel.resetEmailValidation()
                },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible)
                                "Hide password"
                            else
                                "Show password"
                        )
                    }
                }
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    when {
                        name.isEmpty() ->
                            errorMessage = "Please enter your name"
                        email.isEmpty() ->
                            errorMessage = "Please enter your email"
                        password.isEmpty() ->
                            errorMessage = "Please enter a password"
                        password != confirmPassword ->
                            errorMessage = "Passwords don't match"
                        password.length < 6 ->
                            errorMessage = "Password must be at least 6 characters"
                        else -> {
                            errorMessage = ""
                            viewModel.validateEmail(email)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = authState !is AuthState.Loading &&
                        emailValidationState !is EmailValidationState.Validating
            ) {
                when {
                    authState is AuthState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    emailValidationState is EmailValidationState.Validating -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validating Email...", fontSize = 16.sp)
                    }
                    else -> {
                        Text("Sign Up", fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text("Already have an account? ")
                Text(
                    text = "Login",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onNavigateBack)
                )
            }
        }
    }
}