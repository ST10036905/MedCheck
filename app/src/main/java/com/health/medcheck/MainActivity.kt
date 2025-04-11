package com.health.medcheck

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import com.health.medcheck.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Declare GoogleSignInClient for handling Google sign-ins
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    // Declare FirebaseAuth for managing Firebase authentication
    private lateinit var mAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    // Declare a binding variable for using view binding to access views
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyTheme(this)
        // Gets the shared preferences
        sharedPreferences = getSharedPreferences("Preferences", MODE_PRIVATE)
        // Get saved language preference
        val languageCode = sharedPreferences.getString("LANGUAGE", "en")
        // Update the locale
        val locale = Locale(languageCode!!)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // calls the notification channel
        createNotificationChannel()

        // Enable view binding to inflate layout and access UI elements
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NOW you can animate the button via binding
        binding.getStartedBtn.animate().alpha(1f).setDuration(800).start()

        // Initialize Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance()

        // Set up Google Sign-In options using default sign-in configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id)) // Request the web client ID from resources
            .requestEmail() // Request user's email address
            .build()

        // Initialize the GoogleSignInClient with the Google Sign-In options
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check if the user is already signed in with Firebase Authentication
        val user = Firebase.auth.currentUser
        if (user != null) {
            // If user is logged in, display their email in the welcome TextView
            binding.textWelcomeUser.text = getString(R.string.welcome_user, user.email ?: "")
        } else {
            // If user is not logged in, display a default welcome message
            binding.textWelcomeUser.text = getString(R.string.welcome_guest)
        }

        // Set a click listener on the "Get Started" button to navigate to the Welcome activity
        binding.getStartedBtn.setOnClickListener {
            // Prepare both activities for transition
            val intent = Intent(this, Login::class.java)

            // Enable transition in both windows
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                // Convert to AndroidX Pairs explicitly
                androidx.core.util.Pair.create(binding.logo, "logo_transition"),
                androidx.core.util.Pair.create(binding.textWelcome, "welcome_text_transition"),
                androidx.core.util.Pair.create(binding.getStartedBtn, "button_transition")
            ).apply {
                // Additional transition customization
                toBundle()?.putBoolean("EXTRA_TRANSITION_ANIMATION", true)
            }

            // Ensure hardware acceleration is enabled
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )

            // Start with transition
            startActivity(intent, options.toBundle())

            // Optional: Disable default override
            overridePendingTransition(0, 0)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medication Reminder Channel"
            val descriptionText = "Channel for medication reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("medication_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}