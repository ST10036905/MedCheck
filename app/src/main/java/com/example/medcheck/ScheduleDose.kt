package com.example.medcheck

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityScheduleDoseBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class ScheduleDose : AppCompatActivity() {

    // Binding variable for view binding
    private lateinit var binding: ActivityScheduleDoseBinding
    // Reference to Firebase Database
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityScheduleDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference to "medicines"
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

        // Get the medicine ID passed from the AddMedicine activity
        val medicineId = intent.getStringExtra("medicineId") ?: return

        // Set up onClickListener to open TimePickerDialog for dose input
        binding.timeTakenInput.setOnClickListener {
            openTimePicker() // Opens a dialog to select time
        }

        // Populate the "How Often" spinner with dosage options
        val dosageOptions = arrayOf(
            "Select an option",
            "Every 4 Hours",
            "Every 8 Hours",
            "Every 12 Hours",
            "Daily",
            "Weekly"
        )
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dosageOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.oftenSpinner.adapter = spinnerAdapter // Set the adapter for the spinner

        // Set up click listener for the "Schedule Dose" button
        binding.scheduleDoseBtn.setOnClickListener {
            // Get values from input fields
            val doseTime = binding.timeTakenInput.text.toString()
            val howOften = binding.oftenSpinner.selectedItem.toString()
            val howMany = binding.howManyInput.text.toString()

            // Validate inputs to ensure all fields are filled
            if (doseTime.isEmpty() || howMany.isEmpty() || howOften == "Select an option") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Store dose details in Firebase
                storeScheduledDoseInFirebase(medicineId, doseTime, howOften, howMany) // Pass the medicine ID
            }
        }
    }

    // Method to open a TimePickerDialog for selecting time
    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create and show the TimePickerDialog
        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute) // Format the time
            binding.timeTakenInput.setText(time) // Set the selected time in the input field
        }, hour, minute, true)
        timePickerDialog.show() // Display the dialog
    }

    // Method to store scheduled dose information in Firebase
    private fun storeScheduledDoseInFirebase(medicineId: String, doseTime: String, howOften: String, howMany: String) {
        val doseData = HashMap<String, String>()
        doseData["doseTime"] = doseTime // Store the dose time
        doseData["howOften"] = howOften // Store how often the dose is taken
        doseData["howMany"] = howMany // Store how many doses to take

        // Push dose data to Firebase under the specified medicine ID
        databaseReference.child(medicineId).child("schedules").push().setValue(doseData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Dose scheduled successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to Medicine Details activity after successful scheduling
                    val intent = Intent(this, MedicineDetailsActivity::class.java)
                    intent.putExtra("medicineId", medicineId) // Pass the medicine ID
                    startActivity(intent) // Start the Medicine Details activity
                } else {
                    Toast.makeText(this, "Failed to schedule dose", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
