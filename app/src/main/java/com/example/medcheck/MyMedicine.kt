package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import android.widget.EditText
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MyMedicine : AppCompatActivity() {
	
	private lateinit var binding:ActivityMyMedicineBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_my_medicine)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}

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
		
		//-------------------------------------------------------------------------------------------
		//for the navigation bar at the bottom.
		/**
		 * when an icon is clicked,the chosen activity is started (startActivoty) and
		 * the user is sent to their  chosen screen. For
		 * example: User clicks Today,
		 * the taken medication activity starts, showing
		 * the user the taken medication screen of today.
		 */


//------------------------------------------------------------------------------------------------------
		
	//for med details overview
		
		// Retrieve the passed extras from the intent
		val medicineName = intent.getStringExtra("EXTRA_MEDICINE_NAME")
		val dosage = intent.getStringExtra("EXTRA_STRENGTH")
		val frequency = intent.getStringExtra("EXTRA_FREQUENCY")
		
		// Find the EditTexts in the layout
		val medicineNameEditText: EditText = findViewById(R.id.textView2)
		val frequencyEditText: EditText = findViewById(R.id.textView3)
		
		// Set the retrieved values in the EditTexts
		medicineNameEditText.setText(medicineName)
		frequencyEditText.setText(frequency)  // Assuming frequency goes into the second EditText
	}
}