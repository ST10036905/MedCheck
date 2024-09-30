package com.example.medcheck

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.EditText
import com.example.medcheck.databinding.ActivityMedicineDetailsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class MedicineDetailsActivity : AppCompatActivity() {
	
	/**
	 *
	 */
	
	//declaring the edit texts for name and strenghts of medicine
	private lateinit var medicineNameEditText: EditText
	private lateinit var medicineStrengthEditText: EditText
	
	private lateinit var binding: ActivityMedicineDetailsBinding
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_medicine_details)
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
		//heading to the my medicine scree after button click

		val to_my_medicine_screen_button: Button = findViewById(R.id.stopTakingBtn)

		
		// Setting an OnClickListener on the button
		//to_my_medicine_screen_button.setOnClickListener {
			// Creating an Intent to move from MainActivity to NextActivity
			val intent = Intent(this, MyMedicine::class.java)
			
			// and then the My Medicine 'MyMedicine' screen starts (shows the saved medication, not the medication details)
			startActivity(intent)
			
			
		}
//-----------------------------------------------------------------------------------------------
		/*
		adding a button click that sends the user
		to the refill screen after they click on the + button next
		to "add a refill reminder' in the third cardview
		 */
		
		

		
		// Call the method to retrieve data from Firebase
		//retrieveMedicationData("studentId123")  // Replace with the actual student ID

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
	}
	

