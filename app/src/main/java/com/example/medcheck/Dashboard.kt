package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityDashboardBinding
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.material.bottomnavigation.BottomNavigationView


class Dashboard : AppCompatActivity() {
	/**
	 * This Dashboard activity is designed to:
	 *
	 * Retrieve and display the logged-in user's email (which is the username) and age.
	 * It fetches this data either
	 * from an Intent (passed from the previous Register activity)
	 * or from Firebase Realtime Database.
	 */
	private lateinit var binding: ActivityDashboardBinding
	//Used for accessing the UI elements like EditText for the username and age.
	
	private lateinit var firebaseAuth: FirebaseAuth
	//Used to manage the user authentication.
	
	private lateinit var database: FirebaseDatabase
	
	//for retrieval of email(username) and age interactions
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_dashboard)
		// Initialize ViewBinding
		binding = ActivityDashboardBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		// Initialize Firebase Auth and Database
		firebaseAuth = FirebaseAuth.getInstance()
		database = FirebaseDatabase.getInstance()
		
		// Get the email and age passed from the Register activity
		val email = intent.getStringExtra("email")
		val age = intent.getStringExtra("age")
		
		// Display the email and age in the EditText fields
		binding.tvUsername.setText(email)
		binding.tvAge.setText(age)
		
		
		// Fetch and display user data
		//fetchUserData()
		
		//heading to the AddMedicine scree after button click
		val to_add_medicine_screen_button: Button = findViewById(R.id.btn_add_medication)
		
		// Setting an OnClickListener on the button
		to_add_medicine_screen_button.setOnClickListener {
			// Creating an Intent to move from MainActivity to NextActivity
			val intent = Intent(this, AddMedicine::class.java)
			
			// and then addMedicine ' screen starts (so the user can add )
			startActivity(intent)
			//----------------------------------------------------------------------------\
			// Retrieve the passed data from Add Medicine Activity to display in Active Medication cv
			val medicineName = intent.getStringExtra("EXTRA_MEDICINE_NAME")
			val medicineStrength = intent.getStringExtra("EXTRA_STRENGTH")
			val lastTaken = intent.getStringExtra("EXTRA_FREQUENCY")
			
			// Display the data in the EditText fields
			binding.tvMedicationName.setText(medicineName)
			binding.tvMedicationTime.setText(medicineStrength)
			binding.tvLastTaken.setText(lastTaken)

			
			
		}
		/* private fun fetchUserData() {
		 *//*
		looking at the dashbaord activity username and age,
		 the user sees a dashboard with
		these details and its for user excitement
		 *//*
		val userId = firebaseAuth.currentUser?.uid
		if (userId != null) {
			val userRef = database.getReference("Users").child(userId)
			
			userRef.get().addOnSuccessListener { dataSnapshot ->
				if (dataSnapshot.exists()) {
					val email = dataSnapshot.child("email").getValue(String::class.java)
					val age = dataSnapshot.child("age").getValue(String::class.java)
					
				} else {
					Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
				}
			}.addOnFailureListener {
				Toast.makeText(this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
			}
		}
	} */
	
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
