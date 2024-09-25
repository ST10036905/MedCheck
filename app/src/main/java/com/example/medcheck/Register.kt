package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Initialize ViewBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        // Set the content view to the root view of the binding
        setContentView(binding.root)

        // Set up click listeners for the back button
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }

        // Set up click listener for the submit button
        binding.submitBtn.setOnClickListener()
        {
            val name = binding.textName.text.toString()
            val age = binding.textAge.text.toString()
            val weight = binding.textWeight.text.toString()
            val height = binding.textHeight.text.toString()
            val pass = binding.textPass.text.toString()
            val pass2 = binding.textPass2.text.toString()

            if (name.isEmpty() || age.isEmpty() || weight.isEmpty() || height.isEmpty() || pass.isEmpty() || pass2.isEmpty()) {
                if (pass == pass2) {
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
                else
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }