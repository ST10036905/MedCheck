package com.example.medcheck

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.medcheck.databinding.ActivityScheduleDoseBinding
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Date

class ScheduleDose : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleDoseBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var motionLayout: MotionLayout
    private val REQUEST_NOTIFICATION_PERMISSION = 1
    private var interstitialAd: InterstitialAd? = null
    private lateinit var adView: AdView

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

        // ------------ Logs for medicine name
        Log.d("MedNameDebug", "Received extras: ${intent.extras?.keySet()}")
        Log.d("MedNameDebug", "Medicine name: ${intent.getStringExtra("medicineName")}")

        // Initialize MotionLayout
        motionLayout = binding.motionLayout

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Initialize Banner Ad
        adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        // Set AdListener
        adView.adListener = object : AdListener() {
            override fun onAdClosed() {
                Handler(Looper.getMainLooper()).postDelayed({
                    adView.loadAd(AdRequest.Builder().build())
                }, 60000)
            }
        }

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

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            "ca-app-pub-1252634716456493/9223171327",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Interstitial failed: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    // Set ad show callback
                    ad.setOnPaidEventListener { adValue ->
                        Log.d("AdMob", "Paid event: ${adValue.valueMicros}")
                    }
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            loadInterstitial() // Preload next ad
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.e("AdMob", "Ad failed to show: ${adError.message}")
                        }
                    }
                }
            }
        )
    }


    // Call this after successful dose save
    private fun showInterstitial() {
        interstitialAd?.let { ad ->
            ad.show(this)
        } ?: run {
            Log.d("AdMob", "Ad wasn't ready")
            loadInterstitial()
        }
    }

    // Call this when you want to show the ad (e.g., after saving a dose)
    private fun afterDoseSaved() {
        if (interstitialAd != null) {
            showInterstitial()
        } else {
            loadInterstitial()
        }
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
            Log.d("ScheduleDose", "Schedule button clicked")
            //motionLayout.transitionToEnd()
            saveMedicationSchedule(medicineId)
            motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                    Log.d("ScheduleDose", "Transition completed")
                    saveMedicationSchedule(medicineId)
                }
                override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {}
                override fun onTransitionChange(motionLayout: MotionLayout, startId: Int, endId: Int, progress: Float) {}
                override fun onTransitionTrigger(motionLayout: MotionLayout, triggerId: Int, positive: Boolean, progress: Float) {}
            })
        }
    }

    private fun saveMedicationSchedule(medicineId: String) {
        // Get all fields first
        val doseTime = binding.timeTakenInput.text.toString()
        val howOften = binding.oftenSpinner.text.toString()
        val howMany = binding.howManyInput.text.toString()

        if (doseTime.isEmpty() || howMany.isEmpty() || howOften.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // First try to get name from intent, then from database if needed
        val medicineName = intent.getStringExtra("medicineName") ?: run {
            // Fetch from database if not in intent
            databaseReference.child(medicineId).child("name").get().addOnSuccessListener { snapshot ->
                val nameFromDb = snapshot.getValue(String::class.java) ?: "Unknown Medication"
                completeScheduleSave(medicineId, doseTime, howOften, howMany, nameFromDb)
            }.addOnFailureListener {
                completeScheduleSave(medicineId, doseTime, howOften, howMany, "Unknown Medication")
            }
            return
        }

        completeScheduleSave(medicineId, doseTime, howOften, howMany, medicineName)

        afterDoseSaved()
    }

    private fun completeScheduleSave(medicineId: String, doseTime: String,
                                     howOften: String, howMany: String, medicineName: String) {
        val doseData = mapOf(
            "doseTime" to doseTime,
            "howOften" to howOften,
            "howMany" to howMany,
            "medicineName" to medicineName
        )

        databaseReference.child(medicineId).child("schedules").push().setValue(doseData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    scheduleNotification(medicineId, doseTime, howOften, howMany, medicineName)
                    navigateToMedicineDetails(medicineId)
                } else {
                    Toast.makeText(this, "Failed to schedule dose", Toast.LENGTH_SHORT).show()
                }
            }

        afterDoseSaved()
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
            // Create proper Pair objects for transition
            val options = androidx.core.app.ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@ScheduleDose,
                *arrayOf(
                    androidx.core.util.Pair.create(binding.toolbar as View, "toolbar_transition"),
                    androidx.core.util.Pair.create(binding.formCard as View, "form_card_transition")
                )
            )
            startActivity(this, options.toBundle())
            finish()
        }
    }

    private fun scheduleNotification(medicineId: String, doseTime: String,
                                     howOften: String, howMany: String, medicineName: String) {
        try {
            val (hour, minute) = doseTime.split(":").map { it.toInt() }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val notificationIntent = Intent(this, MedicationReminderReceiver::class.java).apply {
                putExtra("medicineName", medicineName)
                putExtra("doseAmount", howMany)
                putExtra("notificationId", medicineId.hashCode())
            }

            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                medicineId.hashCode(),
                notificationIntent,
                flags
            )

            // For Android 6.0+ (API 23+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            // Set repeating alarm if needed
            if (howOften != "Once") {
                val interval = when (howOften) {
                    "Every 4 Hours" -> AlarmManager.INTERVAL_HOUR * 4
                    "Every 8 Hours" -> AlarmManager.INTERVAL_HOUR * 8
                    "Every 12 Hours" -> AlarmManager.INTERVAL_HOUR * 12
                    "Daily" -> AlarmManager.INTERVAL_DAY
                    "Weekly" -> AlarmManager.INTERVAL_DAY * 7
                    else -> 0
                }
                if (interval > 0) {
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        interval,
                        pendingIntent
                    )
                }
            }

            Log.d("AlarmDebug", "Alarm set for $medicineName at $doseTime")
        } catch (e: Exception) {
            Log.e("AlarmError", "Failed to set alarm", e)
            Toast.makeText(this, "Error setting reminder", Toast.LENGTH_SHORT).show()
        }

        afterDoseSaved()
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