package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners for the back button
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }

        // Set up click listener for the submit button
        binding.submitBtn.setOnClickListener {
            val email = binding.textEmail.text.toString()
            val age = binding.textAge.text.toString()
            val weight = binding.textWeight.text.toString()
            val height = binding.textHeight.text.toString()
            val pass = binding.textPass.text.toString()
            val pass2 = binding.textPass2.text.toString()

            // Check if any fields are empty
            if (email.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Check if passwords match
                if (pass == pass2) {
                    registerUser(email, age, weight, height, pass)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUser(email: String, age: String, weight: String, height: String, password: String) {
        // Create a new user with Firebase Authentication using email and password
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the user ID from the authentication result
                    val userId = firebaseAuth.currentUser?.uid

                    // Create a user object with the additional data
                    val user = User(email, age, weight, height)

                    // Save the user's additional data to Firebase Realtime Database
                    if (userId != null) {
                        database.getReference("Users").child(userId).setValue(user)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                    // Redirect to Login activity
                                    val intent = Intent(this, Login::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
