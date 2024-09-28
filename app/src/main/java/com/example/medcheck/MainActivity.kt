package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    //declaring variable to hold binding value
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize view binding
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Set the content view to the root view of the binding
        setContentView(binding.root)

        // Get the "Get Started" button using binding
        binding.getStartedBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }
        
        
    }
    
}