package com.example.medcheck

import android.os.Bundle
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.widget.Toast;
import androidx.core.view.WindowInsetsCompat

class Preferences : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preferences)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Declaring the buttons
        val pushNotificationID: RelativeLayout = findViewById(R.id.pushNotificationID)

        // Setting onclick events
        pushNotificationID.setOnClickListener(){
            Toast.makeText(this,"Export data is not available yet in your region",Toast.LENGTH_SHORT).show()
        }
    }
}