package com.example.medcheck

import android.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityAddMedicineBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddMedicine : AppCompatActivity() {
    // Declare binding variable
    private var binding: ActivityAddMedicineBinding? = null
    private var databaseReference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge mode
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

        // Populate the Spinner with options
        val frequencyOptions = arrayOf("Select an option", "Scheduled Dose", "As Needed")
        val spinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, frequencyOptions)
        spinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding!!.frequencySpinner.adapter = spinnerAdapter

        // Variable to store the selected frequency
        val selectedFrequency = arrayOf("")

        // Set up the Spinner item selection listener
        binding!!.frequencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (position > 0) {
                        selectedFrequency[0] = frequencyOptions[position]
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        // Set up click listener for the save button
        binding!!.saveMedicationBtn.setOnClickListener { v ->
            val name = binding!!.nameInput.text.toString()
            val dosage = binding!!.strenghtInput.text.toString()
            if (name.isEmpty() || dosage.isEmpty() || selectedFrequency[0].isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Store medicine details in Firebase and navigate accordingly
                storeMedicineInFirebase(name, dosage, selectedFrequency[0])
            }
        }
    }

    private fun storeMedicineInFirebase(name: String, dosage: String, frequency: String) {
        // Create a unique key for each medicine entry
        val id = databaseReference!!.push().key
        val medicineData: MutableMap<String, String> = HashMap()
        medicineData["name"] = name
        medicineData["dosage"] = dosage
        medicineData["frequency"] = frequency

        if (id != null) {
            databaseReference!!.child(id).setValue(medicineData)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Medicine added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Redirect based on selected frequency
                        if (frequency == "Scheduled Dose") {
                            // Navigate to ScheduleDose activity
                            val scheduleDoseIntent =
                                Intent(
                                    this,
                                    ScheduleDose::class.java
                                )
                            startActivity(scheduleDoseIntent)
                        } else if (frequency == "As Needed") {
                            // Navigate to MyMedicine activity
                            val myMedicineIntent =
                                Intent(
                                    this,
                                    MyMedicine::class.java
                                )
                            startActivity(myMedicineIntent)
                        }
                    } else {
                        Toast.makeText(this, "Failed to add medicine", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }
    }
}