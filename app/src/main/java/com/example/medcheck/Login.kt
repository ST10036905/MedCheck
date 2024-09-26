package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)

        // Set the content view to the root view of the binding
        setContentView(binding.root)

        // Set up click listeners for forgot password up link
        binding.forgotPasswordLink.setOnClickListener {
            val intent = Intent(this, Preferences::class.java)
            startActivity(intent)
        }

        // Set up click listeners for the sign up link
        binding.signUpLink.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        // Set up click listeners for the back button
        val backBtn = binding.backBtn
        backBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }

        binding.submitBtn.setOnClickListener()
        {
            val name = binding.nameTxt.text.toString()
            val pass = binding.passwordTxt.text.toString()
            if (name.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val intent = Intent(this, AddMedicine::class.java)
                startActivity(intent)
            }
        }
    }
}