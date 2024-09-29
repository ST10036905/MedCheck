package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityAddMedicineBinding

class AddMedicine : AppCompatActivity() {

    // Declare binding variable
    private lateinit var binding: ActivityAddMedicineBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge mode
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)

        // Set the content view to the root view of the binding
        setContentView(binding.root)

        // Populate the Spinner with options
        val frequencyOptions = arrayOf("Select an option", "Scheduled Dose", "As Needed")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencyOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.frequencySpinner.adapter = spinnerAdapter

        // Variable to store the selected frequency
        var selectedFrequency = ""

        // Set up the Spinner item selection listener
        binding.frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> { // "Scheduled Dose" selected
                        selectedFrequency = "Scheduled Dose"
                    }
                    2 -> { // "As Needed" selected
                        selectedFrequency = "As Needed"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing if no item is selected
            }
        }

        // Set up click listener for the save button
        binding.saveMedicationBtn.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val dosage = binding.strenghtInput.text.toString()

            if (name.isEmpty() || dosage.isEmpty() || selectedFrequency.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Medicine added successfully", Toast.LENGTH_SHORT).show()

                // Navigate based on the selected frequency
                val intent = when (selectedFrequency) {
                    "Scheduled Dose" -> Intent(this, ScheduleDose::class.java)
                    "As Needed" -> Intent(this, MainActivity::class.java) // Dashboard activity for "As Needed"
                    else -> return@setOnClickListener
                }

                // Pass medicine details to the next activity
                intent.putExtra("EXTRA_MEDICINE_NAME", name)
                intent.putExtra("EXTRA_STRENGTH", dosage)
                intent.putExtra("EXTRA_FREQUENCY", selectedFrequency)
                startActivity(intent)
                
                //for the dashboard
                
                val DashboardIntent = Intent(this, Dashboard::class.java)
                DashboardIntent.putExtra("EXTRA_MEDICINE_NAME", name)
                DashboardIntent.putExtra("EXTRA_STRENGTH", dosage)
                DashboardIntent.putExtra("EXTRA_FREQUENCY", selectedFrequency)
                
                startActivity(DashboardIntent)
            }
        }
    }
}
