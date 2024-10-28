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

    // Binding for the register activity layout
    private lateinit var binding: ActivityRegisterBinding

    // Firebase Authentication instance
    private lateinit var firebaseAuth: FirebaseAuth

    // Firebase Database instance
    private lateinit var database: FirebaseDatabase

    // Google Sign-In client instance
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
            .requestEmail() // Request the user's email
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso) // Create the GoogleSignInClient

        // Set up click listener for Google Sign-In button
        binding.googleSignInBtn.setOnClickListener {
            signInWithGoogle() // Call the function to sign in with Google
        }

        // Set up click listener for the back button
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java) // Navigate back to the Welcome activity
            startActivity(intent)
        }

        // Set up click listener for the submit button
        binding.submitBtn.setOnClickListener {
            // Retrieve user input from the form
            val email = binding.textEmail.text.toString()
            val age = binding.textAge.text.toString()
            val weight = binding.textWeight.text.toString()
            val height = binding.textHeight.text.toString()
            val pass = binding.textPass.text.toString()
            val pass2 = binding.textPass2.text.toString()

            // Check if all fields are filled
            if (email.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty() && pass.isNotEmpty() && pass2.isNotEmpty()) {
                if (pass == pass2) { // Ensure passwords match

                    // Create user with email and password
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid // Get the new user's ID
                                // Store user data in Firebase Database
                                val userRef = database.getReference("Users").child(userId!!)
                                val userData = mapOf("email" to email, "age" to age, "weight" to weight, "height" to height)
                                userRef.setValue(userData) // Save user data in the database

                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Navigate to the Dashboard after registration
                                val intentDashboard = Intent(this, Dashboard::class.java)
                                intentDashboard.putExtra("email", email)
                                intentDashboard.putExtra("age", age)
                                startActivity(intentDashboard)
                                finish() // Close the current activity
                            } else {
                                // Display error message if registration fails
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    // Notify user if passwords do not match
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Prompt user to fill in all fields if any are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data) // Get sign-in result
        if (task.isSuccessful) {
            val account = task.result // Retrieve the signed-in account
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null) // Get Google Auth credential
                firebaseAuth.signInWithCredential(credential) // Sign in with the credential
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show()
                            // Navigate to the Dashboard after Google Sign-In
                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                            finish() // Close the current activity
                        } else {
                            // Display error message if Google Sign-In fails
                            Toast.makeText(this, "Google Sign-In failed: ${authTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        } else {
            // Display error message if Google Sign-In fails
            Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Function to initiate Google Sign-In
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent // Get the sign-in intent
        googleSignInLauncher.launch(signInIntent) // Launch the sign-in activity
    }
}