package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Dashboard : AppCompatActivity() {

	// Firebase authentication and database references
	private lateinit var auth: FirebaseAuth
	private lateinit var databaseReference: DatabaseReference
	private lateinit var binding: ActivityDashboardBinding

	// UI elements for displaying user info
	private lateinit var emailTextView: TextView
	private lateinit var medicineTextView: TextView


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_dashboard)

		binding = ActivityDashboardBinding.inflate(layoutInflater)
		setContentView(binding.root)

		auth = FirebaseAuth.getInstance()
		databaseReference = FirebaseDatabase.getInstance().getReference("users")

		emailTextView = findViewById(R.id.emailText)
		medicineTextView = findViewById(R.id.medicineText)

		// Get the latest medicine from the intent
		val latestMedicine = intent.getStringExtra("latestMedicine")

		// Display the latest medicine if it exists
		if (!latestMedicine.isNullOrEmpty()) {
			medicineTextView.text = "Recent Medicine: $latestMedicine"
		} else {
			medicineTextView.text = "No latest medicine available"
		}

		fetchUserData()
	}

	//Fetches the user's data from Firebase, including their email and stored medicines.
	private fun fetchUserData() {
		val currentUser = auth.currentUser
		if (currentUser != null) {
			// Display the user's email in the emailTextView
			emailTextView.text = currentUser.email

			// Get the unique user ID from Firebase Authentication
			val userId = currentUser.uid

			// Reference to the user's medicines in the Firebase database
			val userMedicineRef = databaseReference.child(userId).child("medicines")

			// Retrieve the medicine data from the database for the current user
			userMedicineRef.addListenerForSingleValueEvent(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					if (snapshot.exists()) {
						// Fetch the medicine name stored under the "name" key
						val name = snapshot.child("name").getValue(String::class.java)
						if (name != null) {
							medicineTextView.text = name
						} else {
							medicineTextView.text = "No medicine found"
						}
					} else {
						// Show a message if no medicine data is found
						Toast.makeText(this@Dashboard, "No medicine data found", Toast.LENGTH_SHORT).show()
					}
				}

				override fun onCancelled(error: DatabaseError) {
					// Handle errors while fetching data
					Toast.makeText(this@Dashboard, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
				}
			})
		} else {
			// Notify the user if they are not logged in
			Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
		}

		// Button to navigate to AddMedicine activity
		binding.addMedicationBtn.setOnClickListener {
			val intent = Intent(this, AddMedicine::class.java)
			startActivity(intent)
		}

		// Button to navigate to MedicationInformation activity
		binding.knowMoreBtn.setOnClickListener {
			val intent = Intent(this, MedicationInformation::class.java)
			startActivity(intent)
		}

		// Button to navigate to GoogleMap activity for refilling medications
		binding.refillBtn.setOnClickListener {
			val intent = Intent(this, GoogleMap::class.java)
			startActivity(intent)
		}

		//---------------------------------------BOTTOM NAVIGATION SETUP-------------------------------------------------
		// Initialize the BottomNavigationView and handle navigation item selection
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

		// Set a listener for navigation item selection
		bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
			when (item.itemId) {
				R.id.nav_preferences -> {
					// Navigate to the Preferences activity
					startActivity(Intent(this, Preferences::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				R.id.nav_calendar -> {
					// Navigate to the Calendar activity
					startActivity(Intent(this, Calendar::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				R.id.nav_konw_your_med -> {
					// Navigate to the MedicationInformation activity
					startActivity(Intent(this, MedicationInformation::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				R.id.nav_medication -> {
					// Navigate to the MyMedicine activity
					startActivity(Intent(this, MyMedicine::class.java))
					return@setOnNavigationItemSelectedListener true
				}
			}
			false
		}
	}
}
