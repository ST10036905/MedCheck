package com.example.medcheck

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.medcheck.databinding.ActivityScheduleDoseBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ScheduleDose : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleDoseBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var motionLayout: MotionLayout
    private val REQUEST_NOTIFICATION_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable activity transitions
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.sharedElementsUseOverlay = false
        // Postpone transition until views are ready
        postponeEnterTransition()

        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityScheduleDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Start transition when layout is ready
        binding.root.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }

        // Initialize MotionLayout
        motionLayout = binding.motionLayout

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

        // Get medicine ID from intent
        val medicineId = intent.getStringExtra("medicineId") ?: run {
            Toast.makeText(this, "Error: Medicine ID is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up UI components
        setupToolbar()
        setupFrequencyDropdown()
        setupClickListeners(medicineId)
        checkNotificationPermission()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            supportFinishAfterTransition()
        }
    }

    private fun setupFrequencyDropdown() {
        val dosageOptions = listOf(
            "Every 4 Hours",
            "Every 8 Hours",
            "Every 12 Hours",
            "Daily",
            "Weekly"
        )

        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_menu_item,
            dosageOptions
        )

        binding.oftenSpinner.setAdapter(adapter)
        binding.oftenSpinner.setText(dosageOptions.firstOrNull(), false)
    }

    private fun setupClickListeners(medicineId: String) {
        // Time input click listener
        binding.timeTakenInput.setOnClickListener {
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText(getString(R.string.select_time))
                .build()

            timePicker.addOnPositiveButtonClickListener {
                val hour = String.format("%02d", timePicker.hour)
                val minute = String.format("%02d", timePicker.minute)
                binding.timeTakenInput.setText("$hour:$minute")
                motionLayout.transitionToState(R.id.time_selected)
            }
            timePicker.show(supportFragmentManager, "TIME_PICKER")
        }

        // Save button click listener
        binding.scheduleDoseBtn.setOnClickListener {
            motionLayout.transitionToEnd()
            motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                    saveMedicationSchedule(medicineId)
                }
                override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {}
                override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {}
                override fun onTransitionTrigger(motionLayout: MotionLayout, triggerId: Int, positive: Boolean, progress: Float) {}
            })
        }
    }

    private fun saveMedicationSchedule(medicineId: String) {
        val doseTime = binding.timeTakenInput.text.toString()
        val howOften = binding.oftenSpinner.text.toString()
        val howMany = binding.howManyInput.text.toString()

        if (doseTime.isEmpty() || howMany.isEmpty() || howOften.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val doseData = hashMapOf(
            "doseTime" to doseTime,
            "howOften" to howOften,
            "howMany" to howMany
        )

        databaseReference.child(medicineId).child("schedules").push().setValue(doseData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Dose scheduled successfully", Toast.LENGTH_SHORT).show()
                    scheduleNotification(medicineId, doseTime, howOften)
                    navigateToMedicineDetails(medicineId)
                } else {
                    Toast.makeText(this, "Failed to schedule dose", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun navigateToMedicineDetails(medicineId: String) {
        Intent(this, MyMedicine::class.java).apply {
            putExtra("medicineId", medicineId)
            // MODIFIED: Add transition animation
            val options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@ScheduleDose,
                androidx.core.util.Pair(binding.toolbar as View, "toolbar_transition"),
                androidx.core.util.Pair(binding.formCard as View, "form_card_transition")
            )
            startActivity(this, options.toBundle())
            finish()
        }
    }

    private fun scheduleNotification(medicineId: String, doseTime: String, howOften: String) {
        val (hour, minute) = doseTime.split(":").map { it.toInt() }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val intervalMillis = when (howOften) {
            "Every 4 Hours" -> AlarmManager.INTERVAL_HOUR * 4
            "Every 8 Hours" -> AlarmManager.INTERVAL_HOUR * 8
            "Every 12 Hours" -> AlarmManager.INTERVAL_HOUR * 12
            "Daily" -> AlarmManager.INTERVAL_DAY
            "Weekly" -> AlarmManager.INTERVAL_DAY * 7
            else -> AlarmManager.INTERVAL_DAY
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, MedicationReminderReceiver::class.java).apply {
            putExtra("medicineName", "Your Medicine Name")
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            medicineId.hashCode(),
            intent,
            pendingIntentFlags
        )

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            intervalMillis,
            pendingIntent
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission required for reminders", Toast.LENGTH_SHORT).show()
            }
        }
    }
}