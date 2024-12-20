package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    private var selectedFrequency = ""
//--------------------------------ON CREATE
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


        // Handle item selection in the Spinner
        binding!!.frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (position > 0) {
                    selectedFrequency = frequencyOptions[position]
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // OnClickListener to save data
        binding!!.saveMedicationBtn.setOnClickListener {
            val name = binding!!.nameInput.text.toString()
            val dosage = binding!!.strenghtInput.text.toString()

            if (name.isEmpty() || dosage.isEmpty() || selectedFrequency.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                storeMedicineInFirebase(name, dosage, selectedFrequency)
                saveMedicine(name, dosage, selectedFrequency)
            }
        }

    }

    // Method to store the medication data in Firebase
    private fun storeMedicineInFirebase(name: String, dosage: String, frequency: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val id = databaseReference!!.push().key

        val medicineData: MutableMap<String, String> = HashMap()
        medicineData["name"] = name
        medicineData["dosage"] = dosage
        medicineData["frequency"] = frequency
        medicineData["userId"] = userId

        if (id != null) {
            databaseReference!!.child(id).setValue(medicineData)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Medicine added to Firebase", Toast.LENGTH_SHORT).show()
                        val nextActivity = when (selectedFrequency) {
                            "As Needed" -> Dashboard::class.java
                            "Scheduled Dose" -> ScheduleDose::class.java
                            else -> null
                        }

                        if (nextActivity != null) {
                            val intent = Intent(this, nextActivity)
                            intent.putExtra("medicineId", id)
                            startActivity(intent)
                        }
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
        } else {
            Toast.makeText(this, "Failed to add to SQLite", Toast.LENGTH_SHORT).show()
        }
    }
}
