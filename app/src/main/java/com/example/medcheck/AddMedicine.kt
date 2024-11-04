package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityAddMedicineBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.medcheck.Database.DatabaseHandler
import com.google.firebase.auth.FirebaseAuth

class AddMedicine : AppCompatActivity() {
    // Declare binding variable to link XML layout components
    private var binding: ActivityAddMedicineBinding? = null

    // Declare a Firebase database reference
    private var databaseReference: DatabaseReference? = null
    private var databaseHandler: DatabaseHandler? = null
    private lateinit var firebaseAuth: FirebaseAuth // Firebase Auth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding to inflate the layout
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize Firebase database and point to the "medicines" node
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

        // Initialize DatabaseHandler
        databaseHandler = DatabaseHandler(this)

        // Define the options for medication frequency in the Spinner (dropdown)
        val frequencyOptions = arrayOf("Select an option", "Scheduled Dose", "As Needed")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencyOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding!!.frequencySpinner.adapter = spinnerAdapter

        val selectedFrequency = arrayOf("")

        // Handle item selection in the Spinner
        binding!!.frequencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View, position: Int, id: Long
                ) {
                    if (position > 0) {
                        selectedFrequency[0] = frequencyOptions[position]
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing if no item is selected
                }
            }

        // OnClickListener to save data
        binding!!.saveMedicationBtn.setOnClickListener { v ->
            val name = binding!!.nameInput.text.toString()
            val dosage = binding!!.strenghtInput.text.toString()

            if (name.isEmpty() || dosage.isEmpty() || selectedFrequency[0].isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Save to both Firebase and SQLite independently
                storeMedicineInFirebase(name, dosage, selectedFrequency[0])
                saveMedicine(name, dosage, selectedFrequency[0])
            }
        }
    }

    // Method to store the medication data in Firebase
    private fun storeMedicineInFirebase(name: String, dosage: String, frequency: String) {
        val userId = firebaseAuth.currentUser?.uid // Get the current user's ID
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return // Early exit if user is not logged in
        }

        val id = databaseReference!!.push().key

        val medicineData: MutableMap<String, String> = HashMap()
        medicineData["name"] = name
        medicineData["dosage"] = dosage
        medicineData["frequency"] = frequency
        medicineData["userId"] = userId // Add user ID to medicine data

        if (id != null) {
            databaseReference!!.child(id).setValue(medicineData)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Medicine added to Firebase", Toast.LENGTH_SHORT).show()
                        // Optionally navigate after saving
                    } else {
                        Toast.makeText(this, "Failed to add to Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveMedicine(name: String, dosage: String, frequency: String) {
        val result = databaseHandler?.addMedicine(name, dosage, frequency)
        if (result != -1L) {
            Toast.makeText(this, "Medicine added to SQLite", Toast.LENGTH_SHORT).show()
            val myMedicineIntent = Intent(this, MyMedicine::class.java)
            startActivity(myMedicineIntent) // Navigate to MyMedicine after saving
        } else {
            Toast.makeText(this, "Failed to add to SQLite", Toast.LENGTH_SHORT).show()
        }
    }
}
