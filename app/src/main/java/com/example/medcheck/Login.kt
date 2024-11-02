package com.example.medcheck

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    // Declare binding for accessing views and Firebase Auth for authentication
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    // Companion object to hold shared preference keys
    companion object {
        private const val PREFS_NAME = "MedCheckPrefs" // Name of the shared preferences file
        private const val FIRST_TIME_KEY = "isFirstTime" // Key for checking if it's the user's first time
    }

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up ViewBinding to inflate the layout and access views
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences to store/retrieve first-time login status
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Set up the login button click listener for email/password authentication
        binding.submitBtn.setOnClickListener {
            val email = binding.emailTxt.text.toString().trim() // Retrieve and trim the email input
            val pass = binding.passwordTxt.text.toString().trim() // Retrieve and trim the password input

            // Check if email and password are filled in
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Sign in with Firebase using email and password
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // If login is successful, show a message and check if it's the user's first time
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            checkFirstTimeLogin()
                        } else {
                            // If login fails, display an error message
                            Toast.makeText(
                                this,
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                // Show a message if the email or password fields are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the back button to navigate to the Welcome screen
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }
    }

    // Called when the activity becomes visible to the user
    override fun onStart() {
        super.onStart()
        // Check if the user is already signed in with Firebase Auth
        if (firebaseAuth.currentUser != null) {
            checkFirstTimeLogin() // If signed in, check if it's the user's first time
        }
    }

    // Function to check if it's the user's first time logging in and navigate accordingly
    private fun checkFirstTimeLogin() {
        // Retrieve the stored value for whether it's the user's first time using the app
        val isFirstTime = sharedPreferences.getBoolean(FIRST_TIME_KEY, true)

        // Determine which activity to navigate to based on the first-time flag
        val intent = if (isFirstTime) {
            // Navigate to AddMedicineActivity if it's the user's first time
            Intent(this, AddMedicine::class.java)
        } else {
            // Navigate to DashboardActivity if it's not the first time
            Intent(this, Dashboard::class.java)
        }

        // If it's the first time, update the flag to indicate it's no longer the first time
        if (isFirstTime) {
            sharedPreferences.edit().putBoolean(FIRST_TIME_KEY, false).apply()
        }

        // Start the appropriate activity and finish the current one
        startActivity(intent)
        finish()
    }
}
