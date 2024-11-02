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

        // Handle the click event for the "Save" button
        binding!!.saveMedicationBtn.setOnClickListener { v ->
            // Retrieve the medication name and dosage from the input fields
            val name = binding!!.nameInput.text.toString()
            val dosage = binding!!.strenghtInput.text.toString()

            // Check if all fields are filled out
            if (name.isEmpty() || dosage.isEmpty() || selectedFrequency[0].isEmpty()) {
                // Show a message prompting the user to complete all fields
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed to store the medication details in Firebase
                storeMedicineInFirebase(name, dosage, selectedFrequency[0])
            }
        }
    }

    // Method to store the medication data in Firebase
    private fun storeMedicineInFirebase(name: String, dosage: String, frequency: String) {
        // Generate a unique key for each medicine entry in Firebase
        val id = databaseReference!!.push().key

        // Create a map to store the medicine details (name, dosage, frequency)
        val medicineData: MutableMap<String, String> = HashMap()
        medicineData["name"] = name
        medicineData["dosage"] = dosage
        medicineData["frequency"] = frequency
        saveMedicine(name, dosage, frequency)
        // Check if the ID is not null before adding the data to Firebase
        if (id != null) {
            databaseReference!!.child(id).setValue(medicineData)
                .addOnCompleteListener { task: Task<Void?> ->
                    // If the data is successfully added to Firebase
                    if (task.isSuccessful) {
                        // Notify the user that the medicine was added successfully
                        Toast.makeText(this, "Medicine added successfully", Toast.LENGTH_SHORT).show()

                        // Redirect based on the selected frequency option
                        if (frequency == "Scheduled Dose") {
                            // If "Scheduled Dose" is selected, navigate to the ScheduleDose screen and pass the medicine ID
                            val scheduleDoseIntent = Intent(this, ScheduleDose::class.java)
                            scheduleDoseIntent.putExtra("medicineId", id) // Pass medicine ID
                            startActivity(scheduleDoseIntent)
                        } else if (frequency == "As Needed") {
                            // If "As Needed" is selected, navigate to the MyMedicine screen
                            val myMedicineIntent = Intent(this, MyMedicine::class.java)
                            startActivity(myMedicineIntent)
                        }
                    } else {
                        // Notify the user if there was an error adding the medicine
                        Toast.makeText(this, "Failed to add medicine", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    
    //using sql lite to save medicine
    
    private fun saveMedicine(name: String, dosage: String, frequency: String) {
        val result = databaseHandler?.addMedicine(name, dosage, frequency)
        if (result != -1L) {
            Toast.makeText(this, "Medicine added successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to add medicine", Toast.LENGTH_SHORT).show()
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
