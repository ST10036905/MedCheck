package com.example.medcheck

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.medcheck.databinding.ActivityWelcomeBinding
import com.google.firebase.auth.FirebaseAuth

class Welcome : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout to edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize view binding
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Set up button listener for "Create Account"
        binding.createAccBtn.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
        }

        // Set up button listener for "Login"
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
        }

        // Check if the user is logged in when returning to the Welcome screen after logout
        if (firebaseAuth.currentUser != null) {
            firebaseAuth.signOut()  // Ensure the user is logged out on returning to Welcome
            clearFirstTimeLoginPreference()  // Clear first-time login status
        }
    }

    // Clear SharedPreferences for first-time login status
    private fun clearFirstTimeLoginPreference() {
        val sharedPreferences = getSharedPreferences("MedCheckPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }
}
