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
class AddMedicine : AppCompatActivity() {
    // Declare binding variable to link XML layout components
    private var binding: ActivityAddMedicineBinding? = null

    // Declare a Firebase database reference
    private var databaseReference: DatabaseReference? = null
    
    private  var databaseHandler: DatabaseHandler? =  null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up view binding to inflate the layout
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        // Using the root view from binding to set the content view
        setContentView(binding!!.root)

        // Initialize Firebase database and point to the "medicines" node
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")
        
        // Initialize DatabaseHandler
        databaseHandler = DatabaseHandler(this)
        
        // Define the options for medication frequency in the Spinner (dropdown)
        val frequencyOptions = arrayOf("Select an option", "Scheduled Dose", "As Needed")
        // Set up an adapter to handle the list of options and define how each item should look
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencyOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Connect the adapter to the Spinner
        binding!!.frequencySpinner.adapter = spinnerAdapter

        // Create an array to hold the selected frequency option
        val selectedFrequency = arrayOf("")

        // Handle item selection in the Spinner
        binding!!.frequencySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                // This method triggers when a user selects an option from the Spinner
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View, position: Int, id: Long
                ) {
                    // Ensure the selected option is not the first one ("Select an option")
                    if (position > 0) {
                        selectedFrequency[0] = frequencyOptions[position] // Store selected option
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing if no item is selected
                }
            }
        
        // Updated onClickListener to ensure data is saved independently
        binding!!.saveMedicationBtn.setOnClickListener { v ->
            // Retrieve medication details from input fields
            val name = binding!!.nameInput.text.toString()
            val dosage = binding!!.strenghtInput.text.toString()
            
            // Check for empty fields
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
    // Firebase storage method - removed duplicate call to saveMedicine here
    private fun storeMedicineInFirebase(name: String, dosage: String, frequency: String) {
        val id = databaseReference!!.push().key
        
        val medicineData: MutableMap<String, String> = HashMap()
        medicineData["name"] = name
        medicineData["dosage"] = dosage
        medicineData["frequency"] = frequency
        
        if (id != null) {
            databaseReference!!.child(id).setValue(medicineData)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Medicine added to Firebase", Toast.LENGTH_SHORT).show()
                        
                        // Navigation based on frequency
                        val intent = when (frequency) {
                            "Scheduled Dose" -> Intent(this, ScheduleDose::class.java).apply {
                                putExtra("medicineId", id)
                            }
                            "As Needed" -> Intent(this, MyMedicine::class.java)
                            else -> null
                        }
                        intent?.let { startActivity(it) }
                    } else {
                        Toast.makeText(this, "Failed to add to Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    
    
    // SQLite storage method - no changes needed here
    private fun saveMedicine(name: String, dosage: String, frequency: String) {
        val result = databaseHandler?.addMedicine(name, dosage, frequency)
        if (result != -1L) {
            Toast.makeText(this, "Medicine added to SQLite", Toast.LENGTH_SHORT).show()
            val myMedicineIntent = Intent(this, MyMedicine::class.java)
            startActivity(myMedicineIntent)
        } else {
            Toast.makeText(this, "Failed to add to SQLite", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun retrieveMedicines() {
        val cursor = databaseHandler?.getAllMedicines()
        if (cursor?.moveToFirst() == true) {
            do {
                val medicineName = cursor.getString(cursor.getColumnIndexOrThrow("medicineName"))
                val strength = cursor.getString(cursor.getColumnIndexOrThrow("strength"))
                val frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"))
                Toast.makeText(this, "Medicine: $medicineName, Strength: $strength, Frequency: $frequency", Toast.LENGTH_LONG).show()
            } while (cursor.moveToNext())
        }
        cursor?.close()
    }
}
