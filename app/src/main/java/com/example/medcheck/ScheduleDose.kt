package com.example.medcheck

import android.Manifest
import android.content.Context
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.medcheck.databinding.ActivityScheduleDoseBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ScheduleDose : AppCompatActivity() {

    // Binding variable for view binding
    private lateinit var binding: ActivityScheduleDoseBinding
    // Reference to Firebase Database
    private lateinit var databaseReference: DatabaseReference
    private val REQUEST_NOTIFICATION_PERMISSION = 1 // Permission request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityScheduleDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference to "medicines"
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

        val medicineId = intent.getStringExtra("medicineId")
        if (medicineId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: Medicine ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Notification permission request for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }

        binding.timeTakenInput.setOnClickListener {
            openTimePicker()
        }

        // Populate the "How Often" spinner with dosage options
        val dosageOptions = arrayOf(
            "Select an option",
            "Every 4 Hours",
            "Every 8 Hours",
            "Every 12 Hours",
            "Daily",
            "Weekly"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dosageOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.oftenSpinner.adapter = spinnerAdapter

        binding.scheduleDoseBtn.setOnClickListener {
            val doseTime = binding.timeTakenInput.text.toString()
            val howOften = binding.oftenSpinner.selectedItem.toString()
            val howMany = binding.howManyInput.text.toString()

            if (doseTime.isEmpty() || howMany.isEmpty() || howOften == "Select an option") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                storeScheduledDoseInFirebase(medicineId, doseTime, howOften, howMany)
            }
        }
    }

    // Method to open a TimePickerDialog for selecting time
    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.timeTakenInput.setText(time)
        }, hour, minute, true)
        timePickerDialog.show()
    }

    // Method to store scheduled dose information in Firebase
    private fun storeScheduledDoseInFirebase(medicineId: String, doseTime: String, howOften: String, howMany: String) {
        try {
            val doseData = HashMap<String, String>()
            doseData["doseTime"] = doseTime
            doseData["howOften"] = howOften
            doseData["howMany"] = howMany

            databaseReference.child(medicineId).child("schedules").push().setValue(doseData)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Dose scheduled successfully", Toast.LENGTH_SHORT).show()

                        // Navigate to MedicineDetailsActivity with the medicineId
                        val intent = Intent(this, MedicineDetailsActivity::class.java)
                        intent.putExtra("medicineId", medicineId)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Failed to schedule dose", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Log.e("ScheduleDose", "Error saving to Firebase: ${exception.message}")
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("ScheduleDose", "Exception in storeScheduledDoseInFirebase: ${e.message}")
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Schedule the notification based on dose time and frequency
    private fun scheduleNotification(medicineId: String, doseTime: String, howOften: String) {
        val calendar = Calendar.getInstance()
        val (hour, minute) = doseTime.split(":").map { it.toInt() }
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        val intervalMillis = when (howOften) {
            "Every 4 Hours" -> AlarmManager.INTERVAL_HOUR * 4
            "Every 8 Hours" -> AlarmManager.INTERVAL_HOUR * 8
            "Every 12 Hours" -> AlarmManager.INTERVAL_HOUR * 12
            "Daily" -> AlarmManager.INTERVAL_DAY
            "Weekly" -> AlarmManager.INTERVAL_DAY * 7
            else -> AlarmManager.INTERVAL_DAY // Default to daily
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MedicationReminderReceiver::class.java).apply {
            putExtra("medicineName", "Your Medicine Name") // Replace with actual medicine name or retrieve from database if stored
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        // Use medicineId.hashCode() to ensure a unique PendingIntent for each medication
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            medicineId.hashCode(), // Unique ID for each medication
            intent,
            pendingIntentFlags
        )

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, intervalMillis, pendingIntent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                // Schedule notification if the permission is granted
            } else {
                Toast.makeText(this, "Notification permission required for reminders", Toast.LENGTH_SHORT).show()
            }
        }
    }
}