package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.medcheck.databinding.ActivityPreferencesBinding


class Preferences : AppCompatActivity() {

    val termsFragment: Fragment = Terms_and_condition_fragment()
    private lateinit var binding : ActivityPreferencesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferences)

        // Initialize the binding
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //--------------------------Handling the frame transitions ----------------//
        // setting the click listener for the termsAndConditionsMode button
        binding.termsAndConditionsMode.setOnClickListener {
            val termsFragment = Terms_and_condition_fragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, termsFragment)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }


        //----------------------------------End of transitions ------------------//
        // Declaring the buttons
        val pushNotificationID: RelativeLayout = findViewById(R.id.pushNotificationID)

        // Setting onclick events
        pushNotificationID.setOnClickListener(){
            Toast.makeText(this,"Export data is not available yet in your region",Toast.LENGTH_SHORT).show()
        }
    }
}