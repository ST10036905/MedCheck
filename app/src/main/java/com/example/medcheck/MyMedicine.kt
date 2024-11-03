package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.example.medcheck.Database.DatabaseHandler
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper



class MyMedicine : AppCompatActivity() {

	// Declare binding variable and Firebase reference
	private var binding: ActivityMyMedicineBinding? = null
	private var databaseReference: DatabaseReference? = null
	private lateinit var medicineListView: ListView // ListView to display the list of medicines
	private lateinit var medicines: MutableList<String> // Mutable list to hold medicine names
	private lateinit var databaseHandler: DatabaseHandler
	private lateinit var medicineTextView: TextView //textview to view the saved sql lite meds
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Enable edge-to-edge mode
		binding = ActivityMyMedicineBinding.inflate(layoutInflater)
		setContentView(binding!!.root)

		// Initialize Firebase Database reference
		databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

		// Initialize ListView and medicines list
//		medicineListView = binding!!.medicineListView
//		medicines = mutableListOf()

		// Fetch medicines from Firebase
		fetchMedicinesFromFirebase()

		// Set up click listener for the add button to navigate to AddMedicine activity
		binding!!.addAnotherMedicine.setOnClickListener {
			val addMedicineIntent = Intent(this, AddMedicine::class.java)
			startActivity(addMedicineIntent)
		}
		
		// Initialize the database helper and views
		databaseHandler = DatabaseHandler(this)
		        val retrieveButton = findViewById<Button>(R.id.viewMedicineBtn)
		//medicineListView = findViewById(R.id.medicineListView)
		medicineTextView = findViewById(R.id.medicineView_tv)
//click listener to retrieve sql lite data for viewing the saved medication
//		binding!!.viewMedicineBtn.setOnClickListener{
//
//		}
		retrieveButton.setOnClickListener {
			val cursor = databaseHandler.getAllMedicines()
			retrieveMedicines()
		}

		//clearing the medicine
		val clearMedicineBtn = findViewById<Button>(R.id.clearMedicineBtn)
		// Click listener to clear the TextView
		clearMedicineBtn.setOnClickListener {
			medicineTextView.text = ""  // Clears the TextView content
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
	
	//sql lite : retreiving from the database after the user taps on the View button.
	private fun displayData(cursor: Cursor) {
		// Display the retrieved data in the TextView
		// Create a list to store data
		val data = mutableListOf<String>()
		
		// Loop through the cursor and retrieve data
		if (cursor.moveToFirst()) {
			do {
				// Assuming your table has a "name" column; adjust the column name accordingly
				val medicineName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
				data.add(medicineName)
			} while (cursor.moveToNext())
		}
		
		cursor.close() // Close the cursor after use
		
		// Display the data in the TextView
		medicineTextView.text = if (data.isNotEmpty()) {
			data.joinToString("\n")
		} else {
			"No data found"
		}
	}
	
	//more sql lite
	private fun retrieveMedicines() {
		val cursor = databaseHandler.getAllMedicines()
		val medicinesList = StringBuilder()
		
		if (cursor?.moveToFirst() == true) {
			do {
				val medicineName = cursor.getString(cursor.getColumnIndexOrThrow("medicineName"))
				val strength = cursor.getString(cursor.getColumnIndexOrThrow("strength"))
				val frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"))
				
				// Format each record
				medicinesList.append("Medicine: $medicineName\nStrength: $strength\nFrequency: $frequency\n\n")
			} while (cursor.moveToNext())
		} else {
			medicinesList.append("No medicines found.")
		}
		
		cursor?.close()
		
		// Display retrieved data in the TextView
		medicineTextView.text = medicinesList.toString()
	}
}
