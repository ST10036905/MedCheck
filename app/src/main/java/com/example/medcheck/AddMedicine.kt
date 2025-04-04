package com.example.medcheck

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.example.medcheck.databinding.ActivityAddMedicineBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.medcheck.Database.DatabaseHandler
import com.google.firebase.auth.FirebaseAuth

class AddMedicine : AppCompatActivity() {
    private lateinit var binding: ActivityAddMedicineBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseHandler: DatabaseHandler
    private var selectedFrequency: String = ""
    private var isEditMode = false
    private var medicineId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up window transitions before super.onCreate()
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            sharedElementsUseOverlay = false  // Add this line

            // Simplify the transition setup
            sharedElementEnterTransition = MaterialContainerTransform().apply {
                duration = 300L
                scrimColor = Color.TRANSPARENT
                drawingViewId = android.R.id.content  // Add this line
            }
        }

        super.onCreate(savedInstanceState)
        binding = ActivityAddMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if we're in edit mode
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        if (isEditMode) {
            medicineId = intent.getStringExtra("MEDICINE_ID")
            val name = intent.getStringExtra("MEDICINE_NAME") ?: ""
            val dosage = intent.getStringExtra("MEDICINE_DOSAGE") ?: ""
            val frequency = intent.getStringExtra("MEDICINE_FREQUENCY") ?: ""

            binding.nameInput.setText(name)
            binding.strenghtInput.setText(dosage)
            selectedFrequency = frequency

            // Set the spinner selection
            val frequencyOptions = resources.getStringArray(R.array.frequency_options)
            val index = frequencyOptions.indexOf(frequency)
            if (index >= 0) {
                binding.frequencySpinner.setText(frequencyOptions[index], false)
            }

            // Update UI for edit mode
            supportActionBar?.title = "Edit Medicine"
            binding.saveMedicationBtn.text = "Update"
        }

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("medicines")
        databaseHandler = DatabaseHandler(this)

        setupFrequencySpinner()
        setupSaveButton()
    }

    private fun validateAndSaveMedication() {
        val name = binding.nameInput.text.toString().trim()
        val dosage = binding.strenghtInput.text.toString().trim()

        when {
            name.isEmpty() -> showToast("Please enter medicine name")
            dosage.isEmpty() -> showToast("Please enter dosage")
            selectedFrequency.isEmpty() -> showToast("Please select frequency")
            else -> {
                if (isEditMode) {
                    updateMedicineInFirebase(name, dosage)
                } else {
                    storeMedicineInFirebase(name, dosage)
                }
                saveToLocalDatabase(name, dosage)
            }
        }
    }

    private fun updateMedicineInFirebase(name: String, dosage: String) {
        medicineId?.let { id ->
            val updates = hashMapOf<String, Any>(
                "name" to name,
                "dosage" to dosage,
                "frequency" to selectedFrequency
            )

            databaseReference?.child(id)?.updateChildren(updates)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showToast("Medicine updated")
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        showToast("Failed to update medicine")
                    }
                }
        } ?: showToast("Error: Medicine ID not found")
    }

    private fun setupFrequencySpinner() {
        val frequencyOptions = resources.getStringArray(R.array.frequency_options)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            frequencyOptions
        )

        val frequencyDropdown: AutoCompleteTextView = findViewById(R.id.frequencySpinner)
        frequencyDropdown.setAdapter(adapter)

        frequencyDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedFrequency = frequencyOptions[position]
        }
    }


    private fun setupSaveButton() {
        binding.saveMedicationBtn.setOnClickListener {
            validateAndSaveMedication()
        }
    }


    private fun storeMedicineInFirebase(name: String, dosage: String) {
        val userId = firebaseAuth.currentUser?.uid ?: run {
            showToast("User not logged in")
            return
        }

        val medicineId = databaseReference.push().key ?: return

        val medicineData = hashMapOf(
            "name" to name,
            "dosage" to dosage,
            "frequency" to selectedFrequency,
            "userId" to userId
        )

        databaseReference.child(medicineId).setValue(medicineData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Medicine added to Firebase")
                    navigateToNextScreen(medicineId, name)
                } else {
                    showToast("Failed to add to Firebase")
                }
            }
    }

    private fun navigateToNextScreen(medicineId: String, medicineName: String) {
        when (selectedFrequency) {
            "As Needed" -> {
                startActivity(Intent(this, Dashboard::class.java))
            }
            "Scheduled Dose" -> {
                val intent = Intent(this, ScheduleDose::class.java).apply {
                    putExtra("medicineId", medicineId)
                    putExtra("medicine_name", medicineName)
                }

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    Pair.create(binding.toolbar as View, "toolbar_transition"),
                    Pair.create(binding.nameInputLayout as View, "medicine_name_transition"),
                    Pair.create(binding.saveMedicationBtn as View, "next_button_transition")
                )

                startActivity(intent, options.toBundle())
            }
        }
    }

    private fun saveToLocalDatabase(name: String, dosage: String) {
        val result = databaseHandler.addMedicine(name, dosage, selectedFrequency)
        if (result != -1L) {
            showToast("Medicine added to SQLite")
        } else {
            showToast("Failed to add to SQLite")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}