package com.example.medcheck

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicationReminder", "Received alarm intent")

        // Retrieve all necessary data
        val medicineName = intent.getStringExtra("medicineName") ?: "Medication"
        val doseAmount = intent.getStringExtra("doseAmount") ?: "1 pill"
        val notificationId = intent.getIntExtra("notificationId", medicineName.hashCode())

        checkNotificationChannel(context)

        // Build notification with actions
        val notification = buildNotification(context, medicineName, doseAmount, notificationId)

        // Show notification if permissions are granted
        if (hasNotificationPermission(context)) {
            Log.d("MedicationReminder", "Attempting to show notification")
            try {
                NotificationManagerCompat.from(context).notify(notificationId, notification)
                Log.d("MedicationReminder", "Notification shown")
            } catch (e: SecurityException) {
                Log.e("MedicationReminder", "Notification permission denied", e)
            }
        } else {
            Log.e("MedicationReminder", "Missing notification permission")
        }

        // Handle snooze action
        if (intent.action == "ACTION_SNOOZE") {
            handleSnooze(context, intent)
        }
    }

    private fun checkBatteryOptimization(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationPattern = longArrayOf(0, 500, 200, 500)
            val channel = NotificationChannel(
                "medication_channel",
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                // bypass do not disturb
                setBypassDnd(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                description = "Time to take your medication"
                enableLights(true)
                lightColor = ContextCompat.getColor(context, R.color.m3_primary)
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        context: Context,
        medicineName: String,
        doseAmount: String,
        notificationId: Int
    ): Notification {
        // Intent for "Mark as Taken"
        val takenIntent = Intent(context, TakenMedicine::class.java).apply {
            putExtra("medicineName", medicineName)
            putExtra("doseAmount", doseAmount)
            putExtra("notificationId", notificationId)
            action = "ACTION_TAKEN"
        }

        val takenPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for "Snooze"
        val snoozeIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medicineName", medicineName)
            putExtra("doseAmount", doseAmount)
            putExtra("notificationId", notificationId)
            action = "ACTION_SNOOZE"
        }

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, "medication_channel")
            .setSmallIcon(R.drawable.medcheck_logo)
            .setContentTitle("Time to take your medication")
            .setContentText("$medicineName - $doseAmount")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("It's time to take your $medicineName. Dose: $doseAmount"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .addAction(R.drawable.dose, "Mark as Taken", takenPendingIntent)
            .addAction(R.drawable.snooze, "Snooze (10 min)", snoozePendingIntent)
            .build()
    }

    private fun handleSnooze(context: Context, intent: Intent) {
        val snoozeMillis = 10 * 60 * 1000L // 10 minutes
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medicineName", intent.getStringExtra("medicineName"))
            putExtra("doseAmount", intent.getStringExtra("doseAmount"))
            putExtra("notificationId", intent.getIntExtra("notificationId", 0))
            action = "ACTION_SNOOZE"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            intent.getIntExtra("notificationId", 0) + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + snoozeMillis,
            pendingIntent
        )
    }

    private fun checkNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.getNotificationChannel("medication_channel") ?: run {
                createNotificationChannel(context)
            }
        }
    }

    private fun canShowNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}