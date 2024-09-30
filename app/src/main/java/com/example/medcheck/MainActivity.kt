package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    // Declare FirebaseAuth and GoogleSignInClient
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth

    // Declare view binding variable
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance()

        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        // Initialize Google Sign-In client
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check if user is logged in
        val user = Firebase.auth.currentUser
        if (user != null) {
            // Display user name on welcome TextView
            binding.textWelcome.text = "Welcome, ${user.email}"
        } else {
            // Handle the case where the user is not signed in
            binding.textWelcome.text = "Welcome, Guest"
        }

        // "Get Started" button click listener to navigate to Welcome activity
        binding.getStartedBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }

        // Logout button click listener
        binding.logoutButton.setOnClickListener {
            signOutAndStartSignInActivity()
        }
    }

    // Sign out from Firebase and Google, then navigate to the SignInActivity
    private fun signOutAndStartSignInActivity() {
        mAuth.signOut()

        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // Redirect to SignInActivity after sign out
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}
