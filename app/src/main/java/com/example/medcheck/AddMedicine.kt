package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        // Set up click listeners for the save button
        binding.saveMedicationBtn.setOnClickListener {
            val name = binding.nameInput.text.toString()
            val dosage = binding.strenghtInput.text.toString()
            val frequency = binding.frequencyInput.text.toString()

            if(name.isEmpty() || dosage.isEmpty() || frequency.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Medicine added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ScheduleDose::class.java)
                startActivity(intent)
            }
        }
    }
}