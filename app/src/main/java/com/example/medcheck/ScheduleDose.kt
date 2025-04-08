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
import androidx.lifecycle.lifecycleScope
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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Date
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ScheduleDose : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleDoseBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var motionLayout: MotionLayout
    private val REQUEST_NOTIFICATION_PERMISSION = 1
    private var interstitialAd: InterstitialAd? = null
    private lateinit var adView: AdView
    private var rewardedAd: RewardedAd? = null
    private val REWARDED_AD_UNIT_ID = "ca-app-pub-1252634716456493/9636375922"
    private var wasRewardEarned = false
    private var isAdLoading = false
    private var lastAdLoadTime = 0L

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

        loadRewardedAd()

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

    override fun onDestroy() {
        adView.destroy()
        rewardedAd?.fullScreenContentCallback = null
        rewardedAd = null
        super.onDestroy()
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

    private fun loadRewardedAd() {
        // Don't load if already loading or loaded recently
        if (isAdLoading || System.currentTimeMillis() - lastAdLoadTime < 30000) {
            return
        }

        isAdLoading = true
        val adRequest = AdManagerAdRequest.Builder().build()

        RewardedAd.load(this, REWARDED_AD_UNIT_ID, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMob", "Rewarded ad failed to load: ${adError.message}")
                    rewardedAd = null
                    isAdLoading = false
                    lastAdLoadTime = System.currentTimeMillis()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMob", "Rewarded ad loaded")
                    rewardedAd = ad
                    isAdLoading = false
                    lastAdLoadTime = System.currentTimeMillis()
                }
            })
    }

    private fun showRewardedAd(medicineId: String) {  // Add medicineId parameter
        rewardedAd?.let { ad ->
            wasRewardEarned = false

            // Show loading indicator
            binding.progressBar.visibility = View.VISIBLE
            binding.scheduleDoseBtn.isEnabled = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Reload the next ad
                    loadRewardedAd()

                    // Hide loading indicator
                    binding.progressBar.visibility = View.GONE
                    binding.scheduleDoseBtn.isEnabled = true

                    // Continue with app flow
                    if (wasRewardEarned) {
                        Toast.makeText(this@ScheduleDose,
                            "Thank you for watching!", Toast.LENGTH_SHORT).show()
                    }
                    navigateToMedicineDetails(medicineId)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e("AdMob", "Ad failed to show: ${adError.message}")
                    binding.progressBar.visibility = View.GONE
                    binding.scheduleDoseBtn.isEnabled = true
                    navigateToMedicineDetails(medicineId)
                }
            }

            ad.show(this) { rewardItem ->
                wasRewardEarned = true
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d("AdMob", "User earned reward: $rewardAmount $rewardType")
            }
        } ?: run {
            Log.d("AdMob", "Rewarded ad wasn't ready yet")
            navigateToMedicineDetails(medicineId)
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
        // Show loading state immediately
        binding.progressBar.visibility = View.VISIBLE
        binding.scheduleDoseBtn.isEnabled = false

        // Perform database operations in coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Get all fields first
                val doseTime = withContext(Dispatchers.Main) { binding.timeTakenInput.text.toString() }
                val howOften = withContext(Dispatchers.Main) { binding.oftenSpinner.text.toString() }
                val howMany = withContext(Dispatchers.Main) { binding.howManyInput.text.toString() }

                // Validate on IO thread
                if (doseTime.isEmpty() || howMany.isEmpty() || howOften.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScheduleDose, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                        binding.scheduleDoseBtn.isEnabled = true
                    }
                    return@launch
                }

                // Complete save operation
                completeScheduleSave(medicineId, doseTime, howOften, howMany,
                    intent.getStringExtra("medicineName") ?: fetchMedicineName(medicineId))

                // Show ad on main thread
                withContext(Dispatchers.Main) {
                    if (rewardedAd != null) {
                        showRewardedAd(medicineId)
                    } else {
                        navigateToMedicineDetails(medicineId)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ScheduleDose, "Error saving medication", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.scheduleDoseBtn.isEnabled = true
                }
            }
        }
    }

    private suspend fun fetchMedicineName(medicineId: String): String {
        return try {
            val snapshot = databaseReference.child(medicineId).child("name").get().await()
            snapshot.getValue(String::class.java) ?: "Unknown Medication"
        } catch (e: Exception) {
            "Unknown Medication"
        }
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