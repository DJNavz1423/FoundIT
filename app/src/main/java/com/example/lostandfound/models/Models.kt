package com.example.lostandfound.models

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val type: String = "", // "LOST" or "FOUND"
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageBase64: String = "",
    val category: String = "",
    val contactInfo: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
)

data class ChatRoom(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val postId: String = "",
    val unreadCount: Map<String, Int> = emptyMap()
)

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

