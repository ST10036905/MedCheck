package com.example.medcheck // Adjust this based on your actual package

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.OnCompleteListener

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "MyFirebaseMsgService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Optionally, you can send this token to your server
        sendRegistrationToServer(token)
    }

    fun fetchToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log the token to Logcat
            Log.d(TAG, "FCM Token: $token")
        })
    }

    private fun sendRegistrationToServer(token: String) {
        // Implement this method to send the token to your server if needed
        Log.d(TAG, "Sending token to server: $token")
        // Your code to send the token to the server goes here
    }
}
