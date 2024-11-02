package com.example.medcheck

import android.content.Intent
import android.content.SharedPreferences
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
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Declare GoogleSignInClient for handling Google sign-ins
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    // Declare FirebaseAuth for managing Firebase authentication
    private lateinit var mAuth: FirebaseAuth
    //
    private lateinit var sharedPreferences: SharedPreferences

    // Declare a binding variable for using view binding to access views
    private lateinit var binding: ActivityMainBinding

    // Called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
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


        // Enable view binding to inflate layout and access UI elements
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            binding.textWelcomeUser.text = "Welcome, ${user.email}"
        } else {
            // If user is not logged in, display a default welcome message
            binding.textWelcomeUser.text = "Welcome, Guest"
        }

        // Set a click listener on the "Get Started" button to navigate to the Welcome activity
        binding.getStartedBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent) // Start the Welcome activity
        }

    }

    // Function to sign out the user from Firebase and Google, and then navigate to the Login activity
    private fun signOutAndStartSignInActivity() {
        // Sign out from Firebase Authentication
        mAuth.signOut()

        // Sign out from Google Sign-In and upon completion, navigate to the Login activity
        mGoogleSignInClient.signOut().addOnCompleteListener(this) {
            // After signing out, start the Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish() // Close the current activity so the user can't return with the back button
        }
    }
}
