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

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "MedCheckPrefs"
        private const val FIRST_TIME_KEY = "isFirstTime"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Set up Email/Password login
        binding.submitBtn.setOnClickListener {
            val email = binding.emailTxt.text.toString().trim()
            val pass = binding.passwordTxt.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            checkFirstTimeLogin()
                        } else {
                            Toast.makeText(
                                this,
                                "Authentication failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up back button
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }
    }

    // Check if user is already signed in
    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            checkFirstTimeLogin()
        }
    }

    // Check if it's the user's first time and navigate accordingly
    // Check if it's the user's first time and navigate accordingly
    private fun checkFirstTimeLogin() {
        val isFirstTime = sharedPreferences.getBoolean(FIRST_TIME_KEY, true)

        val intent = if (isFirstTime) {
            // Navigate to AddMedicineActivity if first time
            Intent(this, AddMedicine::class.java)
        } else {
            // Navigate to DashboardActivity otherwise
            Intent(this, Dashboard::class.java)
        }

        if (isFirstTime) {
            sharedPreferences.edit().putBoolean(FIRST_TIME_KEY, false).apply()  // Correct way to update the flag
        }

        startActivity(intent)
        finish()
    }

}
