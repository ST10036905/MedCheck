package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.example.medcheck.Database.DatabaseHandler
import android.database.Cursor
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MyMedicine : AppCompatActivity() {

	// Declare binding variable and Firebase reference
	private var binding: ActivityMyMedicineBinding? = null
	private var databaseReference: DatabaseReference? = null
	private lateinit var medicines: MutableList<String> // Mutable list to hold medicine names
	private lateinit var databaseHandler: DatabaseHandler
	private lateinit var adapter: MedicineAdapter
	private var latestMedicine: String? = null // Variable to hold the latest medicine name
	private var userId: String? = null // Store user ID to filter medicines

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// Enable edge-to-edge mode
		binding = ActivityMyMedicineBinding.inflate(layoutInflater)
		setContentView(binding!!.root)
		// Get the current user's ID
		userId = FirebaseAuth.getInstance().currentUser?.uid
		// Initialize Firebase Database reference
		databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

		// Setup RecyclerView
		adapter = MedicineAdapter(emptyList())
		binding?.medicineRecyclerView?.layoutManager = LinearLayoutManager(this)
		binding?.medicineRecyclerView?.adapter = adapter

		// resizes the nav bar icons depending of the screen size of the phone or device used
		val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
		bottomNavView.itemIconSize = resources.getDimensionPixelSize(R.dimen.icon_size)

		//medicineListView = binding!!.medicineListView
		medicines = mutableListOf()

		// Initialize the database handler
		databaseHandler = DatabaseHandler(this)

		// Fetch medicines from Firebase
		fetchMedicinesFromFirebase()

		// Set up click listener for the add button to navigate to AddMedicine activity
		binding!!.addAnotherMedicine.setOnClickListener {
			val addMedicineIntent = Intent(this, AddMedicine::class.java)
			startActivityForResult(addMedicineIntent, ADD_MEDICINE_REQUEST_CODE)
		}

		// Set up click listener for the retrieve button
		val retrieveButton = findViewById<Button>(R.id.viewMedicineBtn)
		retrieveButton.setOnClickListener {
			retrieveMedicines() // Fetch medicines from SQLite and display them
		}

		// Set up click listener for the clear button
		val clearButton = findViewById<Button>(R.id.clearMedicineBtn)
		clearButton.setOnClickListener {
			clearMedicines() // Clear the medicines and display a toast
		}

		// Set up Bottom Navigation
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
		bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
			when (item.itemId) {
				R.id.nav_calendar -> {
					startActivity(Intent(this, Calendar::class.java))
					true
				}
				R.id.nav_dashboard -> {
					startActivity(Intent(this, Dashboard::class.java))
					true
				}
				R.id.nav_konw_your_med -> {
					startActivity(Intent(this, MedicationInformation::class.java))
					true
				}
				R.id.nav_medication -> {
					startActivity(Intent(this, MyMedicine::class.java))
					true
				}
				else -> false
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		binding = null
	}

	private fun fetchMedicinesFromFirebase() {
		userId?.let { uid ->
			databaseReference?.orderByChild("userId")?.equalTo(uid)
				?.addValueEventListener(object : ValueEventListener {
					override fun onDataChange(dataSnapshot: DataSnapshot) {
						medicines = mutableListOf()
						for (snapshot in dataSnapshot.children) {
							snapshot.child("name").getValue(String::class.java)?.let {
								medicines.add(it)
							}
						}
						adapter = MedicineAdapter(medicines)
						binding?.medicineRecyclerView?.adapter = adapter
						latestMedicine = medicines.lastOrNull()
					}

					override fun onCancelled(databaseError: DatabaseError) {
						Toast.makeText(this@MyMedicine,
							"Failed to load medicines: ${databaseError.message}",
							Toast.LENGTH_SHORT).show()
					}
				})
		} ?: run {
			Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
		}
	}


	// Handle the result when returning from AddMedicine
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == ADD_MEDICINE_REQUEST_CODE && resultCode == RESULT_OK) {
			// Pass latest medicine to Dashboard
			val dashboardIntent = Intent(this, Dashboard::class.java).apply {
				putExtra("latestMedicine", latestMedicine)
			}
			startActivity(dashboardIntent)
		}
	}

	// Define a request code
	companion object {
		private const val ADD_MEDICINE_REQUEST_CODE = 1
	}

	// SQLite: retrieving from the database after the user taps on the View button.
	private fun retrieveMedicines() {
		Log.d("MyMedicine", "retrieveMedicines: Retrieving medicines from SQLite.")
		val cursor: Cursor? = databaseHandler.getAllMedicines()
		val medicinesList = StringBuilder()

		cursor?.use {
			if (it.moveToFirst()) {
				do {
					val medicineName = it.getString(it.getColumnIndexOrThrow("name"))
					val strength = it.getString(it.getColumnIndexOrThrow("strength"))
					val frequency = it.getString(it.getColumnIndexOrThrow("frequency"))

					// Format each record
					medicinesList.append("Medicine: $medicineName\nStrength: $strength\nFrequency: $frequency\n\n")
				} while (it.moveToNext())
				Log.d("MyMedicine", "retrieveMedicines: Data retrieved successfully.")
			} else {
				Log.d("MyMedicine", "retrieveMedicines: No medicines found in SQLite.")
				medicinesList.append("No medicines found.")
			}
		}

		// Show a Toast with retrieved data or update the ListView if desired
		if (medicinesList.isNotEmpty()) {
			Toast.makeText(this, medicinesList.toString(), Toast.LENGTH_LONG).show() // Display medicines in a Toast
		} else {
			Toast.makeText(this, "No medicines found.", Toast.LENGTH_SHORT).show()
		}
	}

	// Function to clear medicines and display a toast message
	private fun clearMedicines() {
		medicines.clear()
		adapter.notifyDataSetChanged()
		Toast.makeText(this, "Medicine cabinet cleared.", Toast.LENGTH_SHORT).show()
	}
}


