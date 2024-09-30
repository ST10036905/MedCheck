package com.example.medcheck
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityDashboardBinding
import com.example.medcheck.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Dashboard : AppCompatActivity() {

	// Firebase references
	private lateinit var auth: FirebaseAuth
	private lateinit var databaseReference: DatabaseReference
	private lateinit var binding: ActivityDashboardBinding

	// UI elements
	private lateinit var emailTextView: TextView
	private lateinit var medicineTextView: TextView

	@SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_dashboard)

		// Initialize Firebase Auth and Database reference
		auth = FirebaseAuth.getInstance()
		databaseReference = FirebaseDatabase.getInstance().getReference("users")

		// Initialize UI elements
		emailTextView = findViewById(R.id.tv_user_email) // Make sure to define this in your layout
		medicineTextView = findViewById(R.id.tv_user_medicine) // Make sure to define this in your layout

		// Fetch and display user info
		fetchUserData()

	}

	private fun fetchUserData() {
		val currentUser = auth.currentUser
		if (currentUser != null) {
			// Display the user's email from FirebaseAuth
			emailTextView.text = currentUser.email

			// Get the user's UID to fetch medicine data
			val userId = currentUser.uid

			// Reference to the user's medicines in the database
			val userMedicineRef = databaseReference.child(userId).child("medicines")

			// Fetch the medicine data from the database
			userMedicineRef.addListenerForSingleValueEvent(object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					if (snapshot.exists()) {
						// Assuming you have a medicine name stored under the "medicineName" key
						val name = snapshot.child("name").getValue(String::class.java)
						if (name != null) {
							medicineTextView.text = name
						} else {
							medicineTextView.text = "No medicine found"
						}
					} else {
						Toast.makeText(this@Dashboard, "No medicine data found", Toast.LENGTH_SHORT).show()
					}
				}

				override fun onCancelled(error: DatabaseError) {
					Toast.makeText(this@Dashboard, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
				}
			})
		} else {
			Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
		}

		binding.knowMoreBtn.setOnClickListener{
			val intent = Intent(this, MedicationInformation::class.java)
			startActivity(intent)
    }


		binding.refillBtn.setOnClickListener{
			val intent = Intent(this, GoogleMap::class.java)
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
		//binding.tvLastTaken.setText( lastTaken)


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

	}
}
