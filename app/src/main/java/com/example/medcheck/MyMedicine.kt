package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.example.medcheck.Database.DatabaseHandler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
		// Initialize progress bar
		binding?.progressBar?.visibility = View.GONE
		// Get the current user's ID
		userId = FirebaseAuth.getInstance().currentUser?.uid
		// Initialize Firebase Database reference
		databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

		// Initialize adapter with click listeners
		adapter = MedicineAdapter(emptyList(),
			onEditClick = { medicine -> editMedicine(medicine) },
			onDeleteClick = { medicine -> deleteMedicine(medicine) }
		)

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

		// Button to navigate to GoogleMap activity for refilling medications
		binding!!.RefillMedicineBtn.setOnClickListener {
			val intent = Intent(this, GoogleMap::class.java)
			startActivity(intent)
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
		binding?.progressBar?.visibility = View.VISIBLE

		userId?.let { uid ->
			databaseReference?.orderByChild("userId")?.equalTo(uid)
				?.limitToFirst(20)
				?.addListenerForSingleValueEvent(object : ValueEventListener {
					override fun onDataChange(dataSnapshot: DataSnapshot) {
						val medicineList = mutableListOf<MedicineAdapter.Medicine>()

						// Process data in background thread
						CoroutineScope(Dispatchers.Default).launch {
							for (snapshot in dataSnapshot.children) {
								val name = snapshot.child("name").getValue(String::class.java) ?: ""
								val dosage = snapshot.child("dosage").getValue(String::class.java) ?: ""
								val frequency = snapshot.child("frequency").getValue(String::class.java) ?: ""
								val id = snapshot.key ?: ""

								medicineList.add(MedicineAdapter.Medicine(id, name, dosage, frequency))
							}

							// Update UI on main thread
							withContext(Dispatchers.Main) {
								adapter.updateList(medicineList)
								latestMedicine = medicineList.lastOrNull()?.name
								binding?.progressBar?.visibility = View.GONE
							}
						}
					}

					override fun onCancelled(databaseError: DatabaseError) {
						binding?.progressBar?.visibility = View.GONE
						Toast.makeText(
							this@MyMedicine,
							"Failed to load medicines: ${databaseError.message}",
							Toast.LENGTH_SHORT
						).show()
					}
				})
		} ?: run {
			binding?.progressBar?.visibility = View.GONE
		}
	}

	private fun editMedicine(medicine: MedicineAdapter.Medicine) {
		val intent = Intent(this, AddMedicine::class.java).apply {
			putExtra("EDIT_MODE", true)
			putExtra("MEDICINE_ID", medicine.id)
			putExtra("MEDICINE_NAME", medicine.name)
			putExtra("MEDICINE_DOSAGE", medicine.dosage)
			putExtra("MEDICINE_FREQUENCY", medicine.frequency)
		}
		startActivityForResult(intent, EDIT_MEDICINE_REQUEST_CODE)
	}

	private fun deleteMedicine(medicine: MedicineAdapter.Medicine) {
		AlertDialog.Builder(this)
			.setTitle("Delete Medicine")
			.setMessage("Are you sure you want to delete ${medicine.name}?")
			.setPositiveButton("Delete") { _, _ ->
				databaseReference?.child(medicine.id)?.removeValue()
					?.addOnSuccessListener {
						Toast.makeText(this, "${medicine.name} deleted", Toast.LENGTH_SHORT).show()
					}
					?.addOnFailureListener { e ->
						Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
					}
			}
			.setNegativeButton("Cancel", null)
			.show()
	}

	companion object {
		private const val ADD_MEDICINE_REQUEST_CODE = 1
		private const val EDIT_MEDICINE_REQUEST_CODE = 2
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


	// Function to clear medicines and display a toast message
	private fun clearMedicines() {
		binding?.progressBar?.visibility = View.VISIBLE
		CoroutineScope(Dispatchers.IO).launch {
			try {
				userId?.let { uid ->
					val task = databaseReference?.orderByChild("userId")?.equalTo(uid)
						?.addListenerForSingleValueEvent(object : ValueEventListener {
							override fun onDataChange(dataSnapshot: DataSnapshot) {
								for (snapshot in dataSnapshot.children) {
									snapshot.ref.removeValue()
								}
							}
							override fun onCancelled(databaseError: DatabaseError) {
								// Handle error in the coroutine
								throw databaseError.toException()
							}
						})
				}

				withContext(Dispatchers.Main) {
					medicines.clear()
					adapter.updateList(emptyList())
					binding?.progressBar?.visibility = View.GONE
					Toast.makeText(
						this@MyMedicine,
						"Medicine cabinet cleared.",
						Toast.LENGTH_SHORT
					).show()
				}
			} catch (e: Exception) {
				withContext(Dispatchers.Main) {
					binding?.progressBar?.visibility = View.GONE
					Toast.makeText(
						this@MyMedicine,
						"Error clearing medicines: ${e.message}",
						Toast.LENGTH_SHORT
					).show()
				}
			}
		}
	}
}
