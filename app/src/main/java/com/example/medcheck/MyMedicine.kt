package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class MyMedicine : AppCompatActivity() {

	// Declare binding variable and Firebase reference
	private var binding: ActivityMyMedicineBinding? = null
	private var databaseReference: DatabaseReference? = null
	private lateinit var medicineListView: ListView // ListView to display the list of medicines
	private lateinit var medicines: MutableList<String> // Mutable list to hold medicine names

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Enable edge-to-edge mode
		binding = ActivityMyMedicineBinding.inflate(layoutInflater)
		setContentView(binding!!.root)

		// Initialize Firebase Database reference
		databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

		// Initialize ListView and medicines list
		medicineListView = binding!!.medicineListView
		medicines = mutableListOf()

		// Fetch medicines from Firebase
		fetchMedicinesFromFirebase()

		// Set up click listener for the add button to navigate to AddMedicine activity
		binding!!.addAnotherMedicine.setOnClickListener {
			val addMedicineIntent = Intent(this, AddMedicine::class.java)
			startActivity(addMedicineIntent)
		}

		//---------------------------------------BOTTOM NAV-------------------------------------------------
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

		// Handle navigation item selection for the bottom navigation bar
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
					// Navigate to About Med Activity
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
		//--------------------------------------------------------------------------------------------------
	}

	// Fetch medicines from Firebase Realtime Database
	private fun fetchMedicinesFromFirebase() {
		// Add a ValueEventListener to the database reference
		databaseReference!!.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(dataSnapshot: DataSnapshot) {
				medicines.clear() // Clear the list before adding new data
				for (snapshot in dataSnapshot.children) {
					val medicineName = snapshot.child("name").getValue(String::class.java) // Get medicine name
					if (medicineName != null) {
						medicines.add(medicineName) // Add medicine name to the list
					}
				}
				// Update ListView with the fetched data
				val adapter = ArrayAdapter(this@MyMedicine, android.R.layout.simple_list_item_1, medicines)
				medicineListView.adapter = adapter
			}

			override fun onCancelled(databaseError: DatabaseError) {
				// Show error message if data retrieval fails
				Toast.makeText(this@MyMedicine, "Failed to load medicines.", Toast.LENGTH_SHORT).show()
			}
		})
	}
}
