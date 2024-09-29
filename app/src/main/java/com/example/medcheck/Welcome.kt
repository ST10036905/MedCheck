package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMainBinding
import com.example.medcheck.databinding.ActivityWelcomeBinding

class Welcome : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        // Initialize view binding
        binding = ActivityWelcomeBinding.inflate(layoutInflater)

        // Set the content view to the root view of the binding
        setContentView(binding.root)

        // Get the "Create Account" button and set up click listener
        val createAccBtn = findViewById<Button>(R.id.createAccBtn)
        createAccBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        // Get the "Login" button and set up click listener
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        loginBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Get the "Back" button and set up click listener
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }
}