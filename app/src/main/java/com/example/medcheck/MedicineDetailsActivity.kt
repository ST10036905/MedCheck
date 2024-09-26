package com.example.medcheck

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent

class MedicineDetailsActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_medicine_details)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
		
		//heading to the my medicine scree after button click
		val to_my_medicine_screen_button: Button = findViewById(R.id.btn_stop_taking_medication)
		
		// Setting an OnClickListener on the button
		to_my_medicine_screen_button.setOnClickListener {
			// Creating an Intent to move from MainActivity to NextActivity
			val intent = Intent(this, MyMedicine::class.java)
			
			// and then the My Medicine 'MyMedicine' screen starts (shows the saved medication, not the medication details)
			startActivity(intent)
			
		}
	}
}