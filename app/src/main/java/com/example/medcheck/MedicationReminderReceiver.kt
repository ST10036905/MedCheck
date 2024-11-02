package com.example.medcheck

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MedicationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve any extra data (e.g., medicine name) if needed
        val medicineName = intent.getStringExtra("medicineName") ?: "Medication Reminder"

        // Create a notification to remind the user to take their medication
        val notification = NotificationCompat.Builder(context, "medication_channel")
            .setSmallIcon(R.drawable.medcheck_logo)  // replace with your app's icon
            .setContentTitle("Medication Reminder")
            .setContentText("It's time to take your medication: $medicineName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismisses the notification when tapped
            .build()

        // Show the notification
        if (ActivityCompat.checkSelfPermission(
                context, // Updated to use `context` here
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(medicineName.hashCode(), notification)
    }
}
