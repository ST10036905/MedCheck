package com.example.medcheck

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityTakenMedicationBinding

class TakenMedication : AppCompatActivity() {

    private lateinit var binding: ActivityTakenMedicationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTakenMedicationBinding.inflate(layoutInflater)



    }
}