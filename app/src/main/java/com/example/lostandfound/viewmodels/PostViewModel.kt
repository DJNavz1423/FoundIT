package com.example.lostandfound.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class PostViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun loadPosts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("PostViewModel", "Loading posts from Firestore...")

                val snapshot = firestore.collection("posts")
                    .whereEqualTo("status", "ACTIVE")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()

                Log.d("PostViewModel", "Found ${snapshot.documents.size} posts")

                val postsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                        Log.d("PostViewModel", "Loaded post: ${post?.title}")
                        post
                    } catch (e: Exception) {
                        Log.e("PostViewModel", "Error parsing post ${doc.id}: ${e.message}")
                        null
                    }
                }

                _posts.value = postsList
                Log.d("PostViewModel", "Total posts loaded: ${postsList.size}")

                if (postsList.isEmpty()) {
                    _errorMessage.value = "No posts found. Create your first post!"
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error loading posts: ${e.message}", e)
                _errorMessage.value = "Error loading posts: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMyPosts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid

                if (userId == null) {
                    Log.e("PostViewModel", "❌ User not logged in!")
                    _errorMessage.value = "User not logged in"
                    _isLoading.value = false
                    return@launch
                }

                Log.d("PostViewModel", "📱 loadMyPosts() called for user: $userId")

                // Try WITHOUT orderBy first (might need index)
                val snapshot = firestore.collection("posts")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                Log.d("PostViewModel", "✅ Query completed. Found ${snapshot.documents.size} documents")

                if (snapshot.documents.isEmpty()) {
                    Log.w("PostViewModel", "⚠️ No documents found for userId: $userId")
                } else {
                    Log.d("PostViewModel", "📄 Documents found:")
                    snapshot.documents.forEach { doc ->
                        Log.d("PostViewModel", "  - ID: ${doc.id}")
                        Log.d("PostViewModel", "    title: ${doc.getString("title")}")
                        Log.d("PostViewModel", "    userId: ${doc.getString("userId")}")
                        Log.d("PostViewModel", "    status: ${doc.getString("status")}")
                    }
                }

                val postsList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val post = doc.toObject(Post::class.java)?.copy(id = doc.id)
                        if (post == null) {
                            Log.e("PostViewModel", "❌ Failed to parse document ${doc.id}")
                        } else {
                            Log.d("PostViewModel", "✅ Parsed: ${post.title}")
                        }
                        post
                    } catch (e: Exception) {
                        Log.e("PostViewModel", "❌ Exception parsing ${doc.id}: ${e.message}", e)
                        null
                    }
                }

                // Sort manually if needed
                val sortedPosts = postsList.sortedByDescending { it.timestamp }

                Log.d("PostViewModel", "🎯 Setting _posts.value to ${sortedPosts.size} posts")
                _posts.value = sortedPosts
                Log.d("PostViewModel", "✅ _posts.value is now: ${_posts.value.size}")

            } catch (e: Exception) {
                Log.e("PostViewModel", "❌ EXCEPTION in loadMyPosts: ${e.message}", e)
                Log.e("PostViewModel", "Exception type: ${e.javaClass.name}")
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d("PostViewModel", "🏁 loadMyPosts() finished. Final count: ${_posts.value.size}")
            }
        }
    }

    fun createPost(
        type: String,
        title: String,
        description: String,
        location: String,
        category: String,
        contactInfo: String,
        imageBase64: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: throw Exception("Not authenticated")

                Log.d("PostViewModel", "Creating post for user: $userId")

                val userDoc = firestore.collection("users").document(userId).get().await()
                val userName = userDoc.getString("displayName") ?: "Unknown"

                Log.d("PostViewModel", "User name: $userName")

                val post = Post(
                    userId = userId,
                    userName = userName,
                    type = type,
                    title = title,
                    description = description,
                    location = location,
                    category = category,
                    contactInfo = contactInfo,
                    imageBase64 = imageBase64,
                    timestamp = System.currentTimeMillis(),
                    status = "ACTIVE"
                )

                val docRef = firestore.collection("posts").add(post).await()
                Log.d("PostViewModel", "Post created successfully with ID: ${docRef.id}")

                onSuccess()
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error creating post: ${e.message}", e)
                onError(e.message ?: "Failed to create post")
            }
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val maxSize = 800
        val width = bitmap.width
        val height = bitmap.height

        val resizedBitmap = if (width > maxSize || height > maxSize) {
            val aspectRatio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int

            if (width > height) {
                newWidth = maxSize
                newHeight = (maxSize / aspectRatio).toInt()
            } else {
                newHeight = maxSize
                newWidth = (maxSize * aspectRatio).toInt()
            }

            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }
}

fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        Log.e("base64ToBitmap", "Error decoding image: ${e.message}")
        null
    }
}