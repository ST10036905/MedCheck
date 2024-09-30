package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase


class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure you add your client ID in the strings.xml
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up click listener for Google Sign-In
        binding.googleSignInBtn.setOnClickListener {
            signInWithGoogle()
        }

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

            if (email.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty() && pass.isNotEmpty() && pass2.isNotEmpty()) {
                if (pass == pass2) {

                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid
                                // Store user data in Firebase Database
                                val userRef = database.getReference("Users").child(userId!!)
                                val userData = mapOf("email" to email, "age" to age, "weight" to weight, "height" to height)
                                userRef.setValue(userData)

                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Send the user to the Dashboard after registration
                                val intentDashboard = Intent(this, Dashboard::class.java)
                                intentDashboard.putExtra("email", email)
                                intentDashboard.putExtra("age", age)
                                startActivity(intentDashboard)
                                finish()
                            } else {
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Google Sign-In failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        } else {
            Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
}



