package com.example.medcheck

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent
import android.widget.ImageButton
import android.widget.EditText
import com.example.medcheck.databinding.ActivityMedicineDetailsBinding

class MedicineDetailsActivity : AppCompatActivity() {
	
	/**
	 *
	 */
	
	//declaring the edit texts for name and strenghts of medicine
	private lateinit var medicineNameEditText: EditText
	private lateinit var medicineStrengthEditText: EditText
	
	private lateinit var binding: ActivityMedicineDetailsBinding
	
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
		//val to_my_medicine_screen_button: Button = findViewById(R.id.btn_stop_taking_medication)
		
		// Setting an OnClickListener on the button
		//to_my_medicine_screen_button.setOnClickListener {
			// Creating an Intent to move from MainActivity to NextActivity
			val intent = Intent(this, MyMedicine::class.java)
			
			// and then the My Medicine 'MyMedicine' screen starts (shows the saved medication, not the medication details)
			startActivity(intent)
			
			
		}
//-----------------------------------------------------------------------------------------------
		/*
		adding a button click that sends the user
		to the refill screen after they click on the + button next
		to "add a refill reminder' in the third cardview
		 */
		
		

		
		// Call the method to retrieve data from Firebase
		//retrieveMedicationData("studentId123")  // Replace with the actual student ID

//-------------------------------------------------------------------------------------------
		//for the navigation bar at the bottom.
		/**
		 * when an icon is clicked,the chosen activity is started (startActivoty) and
		 * the user is sent to their  chosen screen. For
		 * example: User clicks Today,
		 * the taken medication activity starts, showing
		 * the user the taken medication screen of today.
		 */

		
//------------------------------------------------------------------------------------------------------
	}
	


/**
 * showing the view of what the user saved about their medication.
 * Need to show: Name + strenght based on how the user saved it.
 */
// Create a method to retrieve data from Firebase Firestore
/* private fun retrieveMedicationData(studentId: String) {
	// Firestore reference to the student document
	val db = FirebaseFirestore.getInstance()
	val savedMedsRef = db.collection("students").document(studentId)
	
	// Get the document
	savedMedsRef.get().addOnSuccessListener { document ->
		if (document != null && document.exists()) {
			// Retrieve the data from the document
			val firstName = document.getString("first_name")
			val lastName = document.getString("last_name")
			
			// Set the data to the EditTexts
			firstNameEditText.setText(firstName)
			lastNameEditText.setText(lastName)
		} else {
			Log.d("Firestore", "No such document")
		}
	}.addOnFailureListener { exception ->
		Log.d("Firestore", "get failed with ", exception)
	}
} */