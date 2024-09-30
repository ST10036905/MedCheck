package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityTakenMedicationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class TakenMedication : AppCompatActivity() {

    private lateinit var binding: ActivityTakenMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

      
        binding = ActivityTakenMedicationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        
//-------------------------------------------------------------------------------------------
        //for the navigation bar at the bottom.
        /**
         * when an icon is clicked,the chosen activity is started (startActivoty) and
         * the user is sent to their  chosen screen. For
         * example: User clicks Today,
         * the taken medication activity starts, showing
         * the user the taken medication screen of today.
         */
        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_preferences -> {
                    // Navigates to preferences
                    startActivity(Intent(this, Preferences::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

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
}