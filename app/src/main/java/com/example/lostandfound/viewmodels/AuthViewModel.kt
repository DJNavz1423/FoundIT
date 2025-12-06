package com.example.lostandfound.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    private val _emailValidationState = MutableStateFlow<EmailValidationState>(EmailValidationState.Idle)
    val emailValidationState: StateFlow<EmailValidationState> = _emailValidationState

    // List of common valid email domains
    private val validDomains = setOf(
        "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
        "icloud.com", "aol.com", "protonmail.com", "zoho.com",
        "mail.com", "yandex.com", "gmx.com", "live.com",
        "msn.com", "me.com", "mac.com"
    )

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authState.value = if (auth.currentUser != null) {
            AuthState.Authenticated
        } else {
            AuthState.Unauthenticated
        }
    }

    fun validateEmail(email: String) {
        viewModelScope.launch {
            try {
                _emailValidationState.value = EmailValidationState.Validating

                // Basic email format validation
                if (!isValidEmailFormat(email)) {
                    _emailValidationState.value = EmailValidationState.Invalid("Invalid email format")
                    return@launch
                }

                // Extract domain from email
                val domain = email.substringAfter("@").lowercase()

                // Check if domain is in the list of valid domains
                if (!validDomains.contains(domain)) {
                    _emailValidationState.value = EmailValidationState.Invalid(
                        "Email domain not supported. Please use Gmail, Yahoo, Outlook, or other major email providers."
                    )
                    return@launch
                }

                // Check if email already exists in Firebase
                val signInMethods = auth.fetchSignInMethodsForEmail(email).await()
                if (signInMethods.signInMethods?.isNotEmpty() == true) {
                    _emailValidationState.value = EmailValidationState.Invalid(
                        "This email is already registered. Please login instead."
                    )
                    return@launch
                }

                _emailValidationState.value = EmailValidationState.Valid
            } catch (e: Exception) {
                _emailValidationState.value = EmailValidationState.Invalid(
                    e.message ?: "Failed to validate email"
                )
            }
        }
    }

    private fun isValidEmailFormat(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )
        return emailPattern.matcher(email).matches()
    }

    fun resetEmailValidation() {
        _emailValidationState.value = EmailValidationState.Idle
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: throw Exception("User ID not found")

                val user = User(
                    uid = userId,
                    email = email,
                    displayName = name
                )

                firestore.collection("users").document(userId).set(user).await()
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to send reset email")
            }
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class EmailValidationState {
    object Idle : EmailValidationState()
    object Validating : EmailValidationState()
    object Valid : EmailValidationState()
    data class Invalid(val message: String) : EmailValidationState()
}