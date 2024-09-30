package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMedicineDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MedicineDetailsActivity : AppCompatActivity() {
	private var binding: ActivityMedicineDetailsBinding? = null
	private var databaseReference: DatabaseReference? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMedicineDetailsBinding.inflate(layoutInflater)
		setContentView(binding!!.root)

		// Get the medicine ID passed from the previous activity
		val medicineId = intent.getStringExtra("medicineId") ?: return

		// Initialize Firebase Database reference
		databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

		// Retrieve medicine details from Firebase
		retrieveMedicineDetails(medicineId)

		// Set up the ImageButton to navigate to ScheduleDose activity
		val scheduleDoseButton: ImageButton = binding!!.scheduleAnotherDoseBtn
		scheduleDoseButton.setOnClickListener {
			val scheduleDoseIntent = Intent(this, ScheduleDose::class.java)
			scheduleDoseIntent.putExtra("medicineId", medicineId) // Pass the medicine ID
			startActivity(scheduleDoseIntent)
		}

		// Set up the ImageButton to navigate to ScheduleDose activity
		val refillButton: ImageButton = binding!!.refillImageBtn
		refillButton.setOnClickListener {
			val refill = Intent(this, MedicationInformation::class.java)
			refill.putExtra("medicineId", medicineId) // Pass the medicine ID
			startActivity(refill)
		}
	}


	private fun retrieveMedicineDetails(medicineId: String) {
		databaseReference?.child(medicineId)?.addListenerForSingleValueEvent(object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				if (snapshot.exists()) {
					// Get medicine details
					val name = snapshot.child("name").getValue(String::class.java)
					val dosage = snapshot.child("dosage").getValue(String::class.java)
					val frequency = snapshot.child("frequency").getValue(String::class.java)
					val time = snapshot.child("time").getValue(String::class.java)
					// Update UI with retrieved data
					binding?.medicineNameInput?.setText(name)
					binding?.medicineStrengthInput?.setText(dosage)
					binding?.frequencyInput?.setText(frequency)
					binding?.timeInput?.setText(time)
				} else {
					Toast.makeText(this@MedicineDetailsActivity, "Medicine not found", Toast.LENGTH_SHORT).show()
				}
			}

			override fun onCancelled(error: DatabaseError) {
				Toast.makeText(this@MedicineDetailsActivity, "Error retrieving data", Toast.LENGTH_SHORT).show()
			}
		})
	}
}
