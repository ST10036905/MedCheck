package com.example.medcheck

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityScheduleDoseBinding
import java.util.*

class ScheduleDose : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleDoseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = ActivityScheduleDoseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up onClickListener to open TimePickerDialog for dose input
        binding.doseInput.setOnClickListener {
            // Get the current time as default values for the picker
          //  val calendar = Calendar.getInstance()
          //  val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
          //  val currentMinute = calendar.get(Calendar.MINUTE)

            // Launch the TimePickerDialog
            /**
            val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                // Format the selected time into HH:MM format
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.doseInput.setText(formattedTime)
            }, currentHour, currentMinute, true) // Use 24-hour format (change to false for AM/PM format) **/

           // timePicker.show()
        }

        // Populate the "How Often" spinner with options
        val dosageOptions = arrayOf("Select an option", "Every 4 Hours", "Every 8 Hours", "Every 12 Hours", "Daily", "Weekly")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dosageOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.oftenSpinner.adapter = spinnerAdapter

        // Set up click listener for the "Schedule Dose" button
        binding.scheduleDoseBtn.setOnClickListener {
            val doseTime = binding.doseInput.text.toString()
            val howMany = binding.quantityInput.text.toString()

            // Validate inputs
            if (doseTime.isEmpty() || howMany.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                // Show confirmation toast for demonstration
                Toast.makeText(this, "Dose scheduled for $howMany times at $doseTime", Toast.LENGTH_LONG).show()

                // Navigate to MainActivity or another screen
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}

