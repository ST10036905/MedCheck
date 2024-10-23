package com.example.medcheck

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityPreferencesBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView

class Preferences : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding // View binding for this activity

    // Declaring the GoogleSignInClient
    private var mGoogleSignInClient: GoogleSignInClient? = null

    @SuppressLint("MissingInflatedId") // Suppresses warnings for missing inflated IDs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge display mode
        setContentView(R.layout.activity_preferences) // Sets the layout

        // Initialize the binding
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root) // Set content view to the binding root

        //--------- Sign out
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // Request email for Google Sign-In
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso) // Initialize GoogleSignInClient

        // Setting up the logout button click listener
        val logoutMode = findViewById<RelativeLayout>(R.id.logoutRL)
        logoutMode.setOnClickListener {
            signOut() // Call signOut() method when clicked
        }

        // Adjusts the padding for the main view to accommodate system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //--------------------------Handling the frame transitions ----------------//
        // Setting up the click listener for the terms and conditions button
        binding.termsAndConditionsRL.setOnClickListener {
            val termsFragment = Terms_and_condition_fragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, termsFragment) // Replace main layout with Terms Fragment
                .addToBackStack(null) // Add transaction to back stack for navigation
                .commit()
        }

        // Setting up the click listener for the FAQ button
        binding.FAQRL.setOnClickListener {
            val faqFragment = FaqFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, faqFragment) // Replace main layout with FAQ Fragment
                .addToBackStack(null)
                .commit()
        }

        // Setting up the click listener for the Whats New button
        binding.whatsNewRL.setOnClickListener {
            val whatsNew = WhatsNewOnMedCheck()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, whatsNew) // Replace main layout with Whats New Fragment
                .addToBackStack(null)
                .commit()
        }

        //----------------------------------End of transitions ------------------//
        // Declaring the buttons for push notifications
        val pushNotificationID: RelativeLayout = findViewById(R.id.pushNotificationID)
        // Setting onClick event for push notifications
        pushNotificationID.setOnClickListener {
            Toast.makeText(this, "This option will be available soon!", Toast.LENGTH_SHORT).show()
        }

        // Export data option
        val exportDataID: RelativeLayout = findViewById(R.id.exportDataID)
        // Setting onClick event for data export
        exportDataID.setOnClickListener {
            Toast.makeText(this, "This option will be available soon!", Toast.LENGTH_SHORT).show()
        }

        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selection for the bottom navigation bar
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {

                R.id.nav_calendar -> {
                    // Navigate to Calendar Activity
                    startActivity(Intent(this, Calendar::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_dashboard -> {
                    // Navigate to Dashboard Activity
                    startActivity(Intent(this, Dashboard::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_konw_your_med -> {
                    // Navigate to About Med Activity
                    startActivity(Intent(this, MedicationInformation::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_medication -> {
                    // Navigate to Medication Activity
                    startActivity(Intent(this, MyMedicine::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
        //--------------------------------------------------------------------------------------------------
    }

    // Sign-out method using GoogleSignInClient
    private fun signOut() {
        mGoogleSignInClient!!.signOut().addOnCompleteListener(this) {
            // Sign-out successful, navigate back to login or main activity
            val intent = Intent(this@Preferences, Welcome::class.java) // Change this to your login activity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK) // Clear activity stack
            startActivity(intent) // Start the login activity
            finish() // Close the current activity
        }
    }
}
