package com.example.medcheck

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.medcheck.databinding.ActivityWelcomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class Welcome : AppCompatActivity() {

    // Declare binding variable for the welcome screen layout
    private lateinit var binding: ActivityWelcomeBinding

    // Declare Google Sign-In and FirebaseAuth instances (not used in the current version but prepared for future Google Sign-In)
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        // Request code for Google Sign-In (used in the commented-out code)
        private const val RC_SIGN_IN = 9001

        // Constants for SharedPreferences to track first-time login status
        private const val PREFS_NAME = "MedCheckPrefs"
        private const val KEY_FIRST_TIME_LOGIN = "firstTimeLogin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make sure the app takes full control of the window, including edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize view binding to access layout elements
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set the content view to the layout root

        // Initialize Firebase Auth for user authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Automatically redirect the user if they're already logged in
        if (firebaseAuth.currentUser != null) {
            checkFirstTimeLoginAndRedirect() // Call function to check if it's the user's first login
        }

        // Set up button listeners for the "Create Account" button
        binding.createAccBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java) // Redirect to the Register screen
            startActivity(intent)
        }

        // Set up button listener for the "Login" button
        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java) // Redirect to the Login screen
            startActivity(intent)
        }

        // Set up the "Back" button to finish the current activity and go back to the previous screen
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish() // Closes the current activity and returns to the previous one
        }
    }

    // Check if this is the user's first login using SharedPreferences and redirect accordingly
    private fun checkFirstTimeLoginAndRedirect() {
        // Access shared preferences to check the stored login status
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean(KEY_FIRST_TIME_LOGIN, true) // Default is true

        // Create an Intent to navigate to the appropriate screen based on the login status
        val intent = if (isFirstTime) {
            Intent(this, AddMedicine::class.java) // Redirect to AddMedicine if first-time login
        } else {
            Intent(this, Dashboard::class.java) // Redirect to Dashboard if not first-time
        }
        startActivity(intent) // Start the selected activity
        finish() // Close the Welcome activity so it's no longer in the back stack
    }
}
