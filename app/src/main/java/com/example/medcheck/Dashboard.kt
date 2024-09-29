package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Dashboard : AppCompatActivity() {
	/**
	 * This Dashboard activity is designed to:
	 *
	 * Retrieve and display the logged-in user's email (which is the username) and age.
	 * It fetches this data either from an Intent (passed from the previous Register activity)
	 * or from Firebase Realtime Database.
	 */
	private lateinit var binding: ActivityDashboardBinding
	// Used for accessing the UI elements like TextView for the username and age.
	
	private lateinit var firebaseAuth: FirebaseAuth
	// Used to manage the user authentication.
	
	private lateinit var database: FirebaseDatabase
	// For retrieval of email (username) and age interactions.
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()  // Enable edge-to-edge display for immersive UI
		
		setContentView(R.layout.activity_dashboard) // Make sure the layout is correct
		// Initialize ViewBinding
		binding = ActivityDashboardBinding.inflate(layoutInflater)
		setContentView(binding.root)  // Set the content view to the inflated binding
		
		// Set up window insets for proper padding
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		// Initialize Firebase Auth and Database
		firebaseAuth = FirebaseAuth.getInstance()
		database = FirebaseDatabase.getInstance()
		
		// Get the email and age passed from the Register activity
		val email = intent.getStringExtra("email") ?: "No email provided"
		val age = intent.getStringExtra("age") ?: "Unknown age"
		
		
		// Display the email and age in the TextView fields
		binding.tvUsername.setText(email)
		binding.tvAge.setText(age)
		
		
		// Setting up button to navigate to the AddMedicine screen
		val toAddMedicineScreenButton: Button = binding.btnAddMedication
		
		// Setting an OnClickListener on the button
		toAddMedicineScreenButton.setOnClickListener {
			// Creating an Intent to move from Dashboard to AddMedicine activity
			val intent = Intent(this, AddMedicine::class.java)
			// Start the AddMedicine activity
			startActivity(intent)
		}
		
		// Retrieve the passed data from AddMedicine activity to display in Active Medication card view
		val medicineName = intent.getStringExtra("EXTRA_MEDICINE_NAME")
		val medicineStrength = intent.getStringExtra("EXTRA_STRENGTH")
		val lastTaken = intent.getStringExtra("EXTRA_FREQUENCY")
		//setText()
		// Display the data in the TextView fields
		binding.tvMedicationName.setText(medicineName) // Use text property instead of setText for TextView
		binding.tvMedicationTime.setText(medicineStrength)
		binding.tvLastTaken.setText( lastTaken)
	


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

