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
        // Retrieve medicine name from the intent extras
        val medicineName = intent.getStringExtra("medicineName") ?: "Medication Reminder"

        // Create notification to remind the user to take their medication
        val notification = NotificationCompat.Builder(context, "medication_channel")
            .setSmallIcon(R.drawable.medcheck_logo)
            .setContentTitle("Medication Reminder")
            .setContentText("It's time to take your medication: $medicineName.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismisses the notification when tapped
            .build()

        // Check if notification permissions are granted (for Android 13+)
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Display the notification
            NotificationManagerCompat.from(context).notify(medicineName.hashCode(), notification)
        } else {
            // Log or notify that permissions are not available
            // Toast or log statement here if needed
        }
    }
}
