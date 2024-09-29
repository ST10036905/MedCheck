package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMyMedicineBinding
import android.widget.EditText

class MyMedicine : AppCompatActivity() {
	
	private lateinit var binding:ActivityMyMedicineBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_my_medicine)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		//-------------------------------------------------------------------------------------------
		//for the navigation bar at the bottom.
		/**
		 * when an icon is clicked,the chosen activity is started (startActivoty) and
		 * the user is sent to their  chosen screen. For
		 * example: User clicks Today,
		 * the taken medication activity starts, showing
		 * the user the taken medication screen of today.
		 */
		// Check initialization of the bottom navigation
		binding.bottomNavigation.setOnItemSelectedListener { item ->
			when (item.itemId) {
				R.id.nav_pref -> {
					val prefIntent = Intent(this, Preferences::class.java)
					startActivity(prefIntent)
				}
				R.id.nav_today -> {
					val todayIntent = Intent(this, TakenMedication::class.java)
					startActivity(todayIntent)
				}
				R.id.nav_meds -> {
					val medsIntent = Intent(this, MyMedicine::class.java)
					startActivity(medsIntent)
				}
				else -> return@setOnItemSelectedListener false  // Return false for unhandled cases
			}
			true  // Return true to indicate the menu item was handled successfully
		}

//------------------------------------------------------------------------------------------------------
		
	//for med details overview
		
		// Retrieve the passed extras from the intent
		val medicineName = intent.getStringExtra("EXTRA_MEDICINE_NAME")
		val dosage = intent.getStringExtra("EXTRA_STRENGTH")
		val frequency = intent.getStringExtra("EXTRA_FREQUENCY")
		
		// Find the EditTexts in the layout
		val medicineNameEditText: EditText = findViewById(R.id.textView2)
		val frequencyEditText: EditText = findViewById(R.id.textView3)
		
		// Set the retrieved values in the EditTexts
		medicineNameEditText.setText(medicineName)
		frequencyEditText.setText(frequency)  // Assuming frequency goes into the second EditText
	}
}