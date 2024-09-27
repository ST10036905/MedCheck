package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners for forgot password link
        binding.forgotPasswordLink.setOnClickListener {
            val intent = Intent(this, Preferences::class.java)
            startActivity(intent)
        }

        // Set up click listeners for the sign up link
        binding.signUpLink.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        // Set up click listeners for the back button
        binding.backBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }

        binding.submitBtn.setOnClickListener {
            val email = binding.emailTxt.text.toString().trim()
            val pass = binding.passwordTxt.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            // Redirect to AddMedicine activity
                            val intent = Intent(this, AddMedicine::class.java)
                            startActivity(intent)
                            finish() // Prevent navigating back to the login page
                        } else {
                            Toast.makeText(
                                this, "Authentication failed: ${it.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
        override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, AddMedicine::class.java)
            startActivity(intent)
            finish()
        }
    }
}
