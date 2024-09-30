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

    private lateinit var binding: ActivityScheduleDoseBinding
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityScheduleDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")

        // Get the medicine ID passed from the AddMedicine activity
        val medicineId = intent.getStringExtra("medicineId") ?: return

        // Set up onClickListener to open TimePickerDialog for dose input
        binding.timeTakenInput.setOnClickListener {
            openTimePicker()
        }

        // Populate the "How Often" spinner with options
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
        binding.oftenSpinner.adapter = spinnerAdapter

        // Set up click listener for the "Schedule Dose" button
        binding.scheduleDoseBtn.setOnClickListener {
            val doseTime = binding.timeTakenInput.text.toString()
            val howOften = binding.oftenSpinner.selectedItem.toString()
            val howMany = binding.howManyInput.text.toString()

            // Validate inputs
            if (doseTime.isEmpty() || howMany.isEmpty() || howOften == "Select an option") {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Store dose details in Firebase
                storeScheduledDoseInFirebase(medicineId, doseTime, howOften, howMany) // Pass the medicine ID
            }
        }
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val time = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.timeTakenInput.setText(time)
        }, hour, minute, true)
        timePickerDialog.show()
    }

    private fun storeScheduledDoseInFirebase(medicineId: String, doseTime: String, howOften: String, howMany: String) {
        val doseData = HashMap<String, String>()
        doseData["doseTime"] = doseTime
        doseData["howOften"] = howOften
        doseData["howMany"] = howMany

        databaseReference.child(medicineId).child("schedules").push().setValue(doseData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Dose scheduled successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to Medicine Details activity
                    val intent = Intent(this, MedicineDetailsActivity::class.java)
                    intent.putExtra("medicineId", medicineId) // Pass the medicine ID
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to schedule dose", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
