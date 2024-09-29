package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.Toast;
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityPreferencesBinding
import com.example.medcheck.databinding.ActivityTakenMedicationBinding

class Preferences : AppCompatActivity() {
    
    private lateinit var binding: ActivityTakenMedicationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferences)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Declaring the buttons
        val pushNotificationID: RelativeLayout = findViewById(R.id.pushNotificationID)

        // Setting onclick events
        pushNotificationID.setOnClickListener(){
            Toast.makeText(this,"Export data is not available yet in your region",Toast.LENGTH_SHORT).show()
        }
        
        //-------------------------------------------------------------------------------------------
        //for the navigation bar at the bottom.
        /**
         * when an icon is clicked,the chosen activity is started (startActivoty) and
         * the user is sent to their  chosen screen. For
         * example: User clicks Today,
         * the taken medication activity starts, showing
         * the user the taken medication screen of today.
         */
        // Check initialization of the bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_pref -> {
                    val prefIntent = Intent(this, Preferences::class.java)
                    startActivity(prefIntent)
                }
                R.id.nav_today -> {
                    val todayIntent = Intent(this, TakenMedication::class.java)
                    startActivity(todayIntent)
                }
                R.id.nav_meds -> {
                    val medsIntent = Intent(this, MyMedicine::class.java)
                    startActivity(medsIntent)
                }
                else -> return@setOnItemSelectedListener false  // Return false for unhandled cases
            }
            true  // Return true to indicate the menu item was handled successfully
        }

//------------------------------------------------------------------------------------------------------
    }
}