package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMedicineDetailsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MedicineDetailsActivity : AppCompatActivity() {

	// Declare the binding object for ActivityMedicineDetailsBinding to interact with the UI elements
	private lateinit var binding: ActivityMedicineDetailsBinding

	// Firebase database reference to interact with the "medicines" node in the database
	private var databaseReference: DatabaseReference? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Inflate the layout using view binding and set it as the content view
		binding = ActivityMedicineDetailsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Retrieve the medicine ID passed from the previous activity, return if null to prevent errors
		val medicineId = intent.getStringExtra("medicineId") ?: return

		// Initialize the Firebase Database reference to point to the "medicines" node
		databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

		// Retrieve and display the details of the medicine from Firebase
		retrieveMedicineDetails(medicineId)

		// Set up the ImageButton to navigate to the ScheduleDose activity for scheduling a new dose
		val scheduleDoseButton: ImageButton = binding.scheduleAnotherDoseBtn
		scheduleDoseButton.setOnClickListener {
			val scheduleDoseIntent = Intent(this, ScheduleDose::class.java)
			scheduleDoseIntent.putExtra("medicineId", medicineId) // Pass the medicine ID
			startActivity(scheduleDoseIntent)
		}

		// Handle the "Done" button click event to navigate back to the Dashboard
		binding.doneBtn.setOnClickListener {
			val intent = Intent(this, Dashboard::class.java)
			startActivity(intent)
		}

		// Set up the refill button to navigate to MedicationInformation activity to refill the medicine
		val refillButton: ImageButton = binding.refillImageBtn
		refillButton.setOnClickListener {
			val refillIntent = Intent(this, GoogleMap::class.java)
			refillIntent.putExtra("medicineId", medicineId) // Pass the medicine ID
			startActivity(refillIntent)
		}

		// Set up the stop medication button to handle the stopping of medication and deletion of data
		val stopMedicationButton: Button = binding.stopMedicationButton
		stopMedicationButton.setOnClickListener {
			// Show a confirmation dialog before deleting the medicine
			confirmDeletion(medicineId)
		}

		// Set up the bottom navigation bar and handle navigation item selection
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
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
					// Navigate to MedicationInformation Activity
					startActivity(Intent(this, MedicationInformation::class.java))
					return@setOnNavigationItemSelectedListener true
				}
				R.id.nav_medication -> {
					// Navigate to MyMedicine Activity
					startActivity(Intent(this, MyMedicine::class.java))
					return@setOnNavigationItemSelectedListener true
				}
			}
			false
		}
	}

	// Function to confirm deletion of a medicine by invoking the delete function
	private fun confirmDeletion(medicineId: String) {
		deleteMedicineFromDatabase(medicineId)
	}

	// Deletes the medicine data from the Firebase database based on the medicine ID
	private fun deleteMedicineFromDatabase(medicineId: String) {
		databaseReference?.child(medicineId)?.removeValue()?.addOnCompleteListener { task ->
			if (task.isSuccessful) {
				Toast.makeText(this, "Medication stopped and details deleted", Toast.LENGTH_SHORT).show()

				// Debugging message to log the deletion action
				Log.d("DeleteMedicine", "Navigating to Dashboard")

				// Navigate back to the Dashboard after successful deletion
				val intent = Intent(this, Dashboard::class.java)
				startActivity(intent)

				// Close the current activity
				finish()
			} else {
				// Notify the user if the deletion fails
				Toast.makeText(this, "Failed to stop medication", Toast.LENGTH_SHORT).show()
			}
		}
	}

	// Retrieves the medicine details from the Firebase database and updates the UI
	private fun retrieveMedicineDetails(medicineId: String) {
		databaseReference?.child(medicineId)?.addListenerForSingleValueEvent(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				if (snapshot.exists()) {
					// Fetch and display medicine details (name, dosage, frequency) in the UI
					val name = snapshot.child("name").getValue(String::class.java)
					val dosage = snapshot.child("dosage").getValue(String::class.java)
					val frequency = snapshot.child("frequency").getValue(String::class.java)
					binding.medicineNameInput.setText(name)
					binding.medicineStrengthInput.setText(dosage)
					binding.frequencyInput.setText(frequency)
				} else {
					Toast.makeText(this@MedicineDetailsActivity, "Medicine not found", Toast.LENGTH_SHORT).show()
				}
			}

			override fun onCancelled(error: DatabaseError) {
				// Notify the user if an error occurs during data retrieval
				Toast.makeText(this@MedicineDetailsActivity, "Error retrieving data", Toast.LENGTH_SHORT).show()
			}
		})
	}
}
