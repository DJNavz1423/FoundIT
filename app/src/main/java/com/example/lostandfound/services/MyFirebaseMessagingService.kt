package com.example.lostandfound.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lostandfound.MainActivity
import com.example.lostandfound.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM token: $token")

        // Save token to Firestore for current user
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            saveFCMToken(userId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM", "Message received from: ${message.from}")
        Log.d("FCM", "Message data: ${message.data}")

        // Show notification
        message.data.let { data ->
            val title = data["title"] ?: "New Message"
            val body = data["body"] ?: "You have a new message"
            val chatRoomId = data["chatRoomId"]
            val type = data["type"] // "new_message" or "new_contact"

            showNotification(title, body, chatRoomId, type)
        }
    }

    private fun saveFCMToken(userId: String, token: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error saving token: ${e.message}")
            }
    }

    private fun showNotification(title: String, body: String, chatRoomId: String?, type: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "lost_and_found_messages"

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("chatRoomId", chatRoomId)
            putExtra("openChat", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to create this
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}