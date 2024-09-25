package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityScheduleDoseBinding

class ScheduleDose : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleDoseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScheduleDoseBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.scheduleDoseBtn.setOnClickListener {
            val doseTime = binding.doseInput.text.toString()
            val howOften = binding.oftenInput.text.toString()
            val howMany = binding.quantityInput.text.toString()

            // Validate inputs
            if (doseTime.isEmpty() || howOften.isEmpty() || howMany.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else
            {
                // For demonstration, we'll just show a confirmation toast
                Toast.makeText(
                    this,
                    "Dose scheduled for : $howOften, times at $doseTime",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}