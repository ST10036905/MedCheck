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

        // Get the medicine ID passed from the AddMedicine activity
        val medicineId = intent.getStringExtra("medicineId") ?: return

        // Notification permission request for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQUEST_NOTIFICATION_PERMISSION)
            }
        }

        //----------Set up onClickListener to open TimePickerDialog for dose input
        binding.timeTakenInput.setOnClickListener {
            openTimePicker() // Opens a dialog to select time
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
        binding.oftenSpinner.adapter = spinnerAdapter // Set the adapter for the spinner

        // Set up click listener for the "Schedule Dose" button
        binding.scheduleDoseBtn.setOnClickListener {
            // Get values from input fields
            val doseTime = binding.timeTakenInput.text.toString()
            val howOften = binding.oftenSpinner.selectedItem.toString()
            val howMany = binding.howManyInput.text.toString()

            // Validate inputs to ensure all fields are filled
            if (doseTime.isEmpty() || howMany.isEmpty() || howOften == "Select an option") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Store dose details in Firebase
                storeScheduledDoseInFirebase(medicineId, doseTime, howOften, howMany) // Pass the medicine ID
            }
        }
    }

    // Method to open a TimePickerDialog for selecting time
    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create and show the TimePickerDialog
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute) // Format the time
            binding.timeTakenInput.setText(time) // Set the selected time in the input field
        }, hour, minute, true)
        timePickerDialog.show() // Display the dialog
    }

    // Method to store scheduled dose information in Firebase
    private fun storeScheduledDoseInFirebase(medicineId: String, doseTime: String, howOften: String, howMany: String) {
        val doseData = HashMap<String, String>()
        doseData["doseTime"] = doseTime // Store the dose time
        doseData["howOften"] = howOften // Store how often the dose is taken
        doseData["howMany"] = howMany // Store how many doses to take

        // Push dose data to Firebase under the specified medicine ID
        databaseReference.child(medicineId).child("schedules").push().setValue(doseData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Dose scheduled successfully", Toast.LENGTH_SHORT).show()

                    // Schedule the notification
                    scheduleNotification(medicineId, doseTime, howOften)

                    // Navigate to Medicine Details activity after successful scheduling
                    val intent = Intent(this, MedicineDetailsActivity::class.java)
                    intent.putExtra("medicineId", medicineId) // Pass the medicine ID
                    startActivity(intent) // Start the Medicine Details activity
                } else {
                    Toast.makeText(this, "Failed to schedule dose", Toast.LENGTH_SHORT).show()
                }
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
