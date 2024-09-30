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
import java.util.Calendar


class Preferences : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding

    // Declaring the GoogleSignInClient
    private var mGoogleSignInClient: GoogleSignInClient? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferences)

        // Initialize the binding
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //--------- Sign out
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val logoutMode = findViewById<RelativeLayout>(R.id.logoutRL)
        logoutMode.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                signOut()
            }
        })
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //--------------------------Handling the frame transitions ----------------//
        // setting the click listener for the termsAndConditionsMode button
        binding.termsAndConditionsRL.setOnClickListener {
            val termsFragment = Terms_and_condition_fragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, termsFragment)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }

        // setting the click listener for the FAQ button
        binding.FAQRL.setOnClickListener {
            val faqFragment = FaqFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, faqFragment)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }

        // setting the click listener for the FAQ button
        binding.whatsNewRL.setOnClickListener {
            val whatsNew = WhatsNewOnMedCheck()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, whatsNew)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }

        //----------------------------------End of transitions ------------------//
        // Declaring the buttons
        val pushNotificationID: RelativeLayout = findViewById(R.id.pushNotificationID)
        // Setting onclick events
        pushNotificationID.setOnClickListener() {
            Toast.makeText(this, "This option will be available soon!", Toast.LENGTH_SHORT).show()
        }
        // export data
        val exportDataID: RelativeLayout = findViewById(R.id.exportDataID)
        // Setting onclick events
        exportDataID.setOnClickListener() {
            Toast.makeText(this, "This option will be available soon!", Toast.LENGTH_SHORT).show()
        }

        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selection
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

    private fun signOut() {
        mGoogleSignInClient!!.signOut().addOnCompleteListener(this) {
            // Sign-out successful, navigate back to login or main activity
            val intent = Intent(
                this@Preferences,
                Welcome::class.java
            ) // Change this to your login activity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish() // Close current activity
        }
    }

}