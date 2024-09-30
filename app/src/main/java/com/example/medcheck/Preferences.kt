package com.example.medcheck

import android.annotation.SuppressLint
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

    private lateinit var binding : ActivityPreferencesBinding
    @SuppressLint("MissingInflatedId")
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
        binding.termsAndConditionsRL.setOnClickListener {
            val termsFragment = Terms_and_condition_fragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, termsFragment)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }

        // setting the click listener for the FAQ button
        binding.FAQRL.setOnClickListener {
            val faqFragment = FaqFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, faqFragment)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }

        // setting the click listener for the FAQ button
        binding.whatsNewRL.setOnClickListener {
            val whatsNew = WhatsNewOnMedCheck()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, whatsNew)
                .addToBackStack(null)
                .commit()
            startActivity(intent)
        }

        //----------------------------------End of transitions ------------------//
        // Declaring the buttons
        val pushNotificationID: RelativeLayout = findViewById(R.id.pushNotificationID)
        // Setting onclick events
        pushNotificationID.setOnClickListener(){
            Toast.makeText(this,"This option will be available soon!",Toast.LENGTH_SHORT).show()
        }
        // export data
        val exportDataID: RelativeLayout = findViewById(R.id.exportDataID)
        // Setting onclick events
        exportDataID.setOnClickListener(){
            Toast.makeText(this,"This option will be available soon!",Toast.LENGTH_SHORT).show()
        }
    }
}