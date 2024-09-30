package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import com.google.firebase.database.*

class MyMedicine : AppCompatActivity() {
	// Declare binding variable and Firebase reference
	private var binding: ActivityMyMedicineBinding? = null
	private var databaseReference: DatabaseReference? = null
	private lateinit var medicineListView: ListView
	private lateinit var medicines: MutableList<String>

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

		// Set up click listener for the add button
		binding!!.addAnotherMedicine.setOnClickListener {
			val addMedicineIntent = Intent(this, AddMedicine::class.java)
			startActivity(addMedicineIntent)
		}
	}

	private fun fetchMedicinesFromFirebase() {
		databaseReference!!.addValueEventListener(object : ValueEventListener {
			override fun onDataChange(dataSnapshot: DataSnapshot) {
				medicines.clear() // Clear the list before adding new data
				for (snapshot in dataSnapshot.children) {
					val medicineName = snapshot.child("name").getValue(String::class.java)
					if (medicineName != null) {
						medicines.add(medicineName)
					}
				}
				// Update ListView
				val adapter = ArrayAdapter(this@MyMedicine, android.R.layout.simple_list_item_1, medicines)
				medicineListView.adapter = adapter
			}

			override fun onCancelled(databaseError: DatabaseError) {
				Toast.makeText(this@MyMedicine, "Failed to load medicines.", Toast.LENGTH_SHORT).show()
			}
		})
	}
}
