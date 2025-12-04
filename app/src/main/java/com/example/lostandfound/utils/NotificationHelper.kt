package com.example.lostandfound.utils

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

object NotificationHelper {

    // CHANGE THIS → Your Node backend URL
    private const val BACKEND_URL = "https://lostfound-backend-gw39.onrender.com/send-notification"

    // Called after permission is granted in MainActivity
    fun initializeFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                saveFCMToken(token)
            } else {
                Log.e("FCM", "Failed to get FCM token", task.exception)
            }
        }
    }

    private fun saveFCMToken(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "Token saved to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error saving token: ${e.message}")
                firestore.collection("users")
                    .document(userId)
                    .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    /**
     * Sends a push notification using your external Node server.
     * This is the REQUIRED piece for background notifications to work.
     */
    fun sendPushToBackend(
        context: Context,
        token: String,
        title: String,
        body: String,
        chatRoomId: String,
        type: String = "new_message"
    ) {
        val jsonData = JSONObject().apply {
            put("token", token)
            put("title", title)
            put("body", body)
            put("chatRoomId", chatRoomId)
            put("type", type)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            BACKEND_URL,
            jsonData,
            { response ->
                Log.d("Backend", "Notification sent: $response")
            },
            { error ->
                Log.e("Backend", "Failed to send notification: ${error.message}")
            }
        )

        Volley.newRequestQueue(context).add(request)
    }


    /**
     * Call this when sending a message.
     * Instead of writing to Firestore (which does nothing for notifications),
     * we call our free backend which TRIGGERS a real FCM push.
     */
    suspend fun sendMessageNotification(
        context: Context,
        recipientUserId: String,
        senderName: String,
        messageText: String,
        chatRoomId: String
    ) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val recipientDoc = firestore.collection("users")
                .document(recipientUserId)
                .get()
                .await()

            val fcmToken = recipientDoc.getString("fcmToken")

            if (fcmToken != null) {
                Log.d("Notification", "Sending push to backend!")

                sendPushToBackend(
                    context = context,
                    token = fcmToken,
                    title = senderName,
                    body = messageText,
                    chatRoomId = chatRoomId
                )
            } else {
                Log.e("Notification", "Recipient has no FCM token saved")
            }

        } catch (e: Exception) {
            Log.e("Notification", "Error sending notification: ${e.message}")
        }
    }
}