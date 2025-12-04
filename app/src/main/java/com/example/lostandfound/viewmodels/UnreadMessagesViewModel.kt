package com.example.lostandfound.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UnreadMessagesViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _totalUnreadCount = MutableStateFlow(0)
    val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    private val _hasUnreadMessages = MutableStateFlow(false)
    val hasUnreadMessages: StateFlow<Boolean> = _hasUnreadMessages

    init {
        startListeningForUnreadMessages()
    }

    private fun startListeningForUnreadMessages() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("chatRooms")
            .whereArrayContains("participantIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UnreadVM", "Error listening to chat rooms: ${error.message}")
                    return@addSnapshotListener
                }

                var totalUnread = 0

                snapshot?.documents?.forEach { doc ->
                    val unreadMap = doc.get("unreadCount") as? Map<*, *>
                    val userUnread = unreadMap?.get(userId) as? Long ?: 0L
                    totalUnread += userUnread.toInt()
                }

                Log.d("UnreadVM", "Total unread messages: $totalUnread")
                _totalUnreadCount.value = totalUnread
                _hasUnreadMessages.value = totalUnread > 0
            }
    }

    fun markChatAsRead(chatRoomId: String) {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                firestore.collection("chatRooms")
                    .document(chatRoomId)
                    .update("unreadCount.$userId", 0)
                    .addOnSuccessListener {
                        Log.d("UnreadVM", "Marked chat $chatRoomId as read")
                    }
            } catch (e: Exception) {
                Log.e("UnreadVM", "Error marking as read: ${e.message}")
            }
        }
    }

    fun incrementUnreadCount(chatRoomId: String, recipientUserId: String) {
        viewModelScope.launch {
            try {
                val chatRoomRef = firestore.collection("chatRooms").document(chatRoomId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(chatRoomRef)
                    val unreadMap = snapshot.get("unreadCount") as? Map<*, *> ?: emptyMap<String, Int>()
                    val currentCount = (unreadMap[recipientUserId] as? Long)?.toInt() ?: 0

                    val newUnreadMap = unreadMap.toMutableMap()
                    newUnreadMap[recipientUserId] = currentCount + 1

                    transaction.update(chatRoomRef, "unreadCount", newUnreadMap)
                }.addOnSuccessListener {
                    Log.d("UnreadVM", "Incremented unread count for $recipientUserId")
                }
            } catch (e: Exception) {
                Log.e("UnreadVM", "Error incrementing unread: ${e.message}")
            }
        }
    }
}