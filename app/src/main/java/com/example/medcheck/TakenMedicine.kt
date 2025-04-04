package com.example.medcheck

import android.app.AlarmManager
import android.app.PendingIntent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*
import android.content.BroadcastReceiver

class TakenMedicine : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_taken_medicine)

        if (intent?.action == "ACTION_TAKEN") {
            // Handle marking medication as taken
            val medicineName = intent.getStringExtra("medicineName")
            val doseAmount = intent.getStringExtra("doseAmount")

            // Cancel the notification
            val notificationId = intent.getIntExtra("notificationId", 0)
            NotificationManagerCompat.from(this).cancel(notificationId)

            // Save to database that dose was taken
            // ... your implementation here ...

            Toast.makeText(this, "Marked $medicineName ($doseAmount) as taken",
                Toast.LENGTH_SHORT).show()
            finish()
        }

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //----------------Method to handle the notification
        val action = intent.action
        val medicineName = intent.getStringExtra("medicineName") ?: "Medication"

        when (action) {
            "ACTION_TAKEN" -> {
                // Handle medication taken feedback
                Toast.makeText(this, "Medication taken: $medicineName", Toast.LENGTH_SHORT).show()

                // Dismiss the notification
                NotificationManagerCompat.from(this).cancel(medicineName.hashCode())
            }
            "ACTION_SNOOZE" -> {
                // Handle snooze action (e.g., reschedule notification)
                Toast.makeText(this, "Snoozed: $medicineName", Toast.LENGTH_SHORT).show()
                scheduleSnoozeNotification(medicineName)
            }
        }

        // Set today's date and day in the TextView
        val dateTextView = findViewById<TextView>(R.id.date_day)
        val currentDate = getCurrentDate()
        dateTextView.text = currentDate

        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selection for the bottom navigation bar
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    startActivity(Intent(this, Calendar::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_konw_your_med -> {
                    startActivity(Intent(this, MedicationInformation::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_medication -> {
                    startActivity(Intent(this, MyMedicine::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        //--------------------------------------------------------------------------------------------------
    }

    private fun scheduleSnoozeNotification(medicineName: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeIntent = Intent(this, MedicationReminderReceiver::class.java).apply {
            putExtra("medicineName", medicineName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            medicineName.hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm for 15 minutes from now
        val triggerTime = System.currentTimeMillis() + (15 * 60 * 1000)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
    // Function to get the current date in "EEEE, MMMM d, yyyy" format
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
