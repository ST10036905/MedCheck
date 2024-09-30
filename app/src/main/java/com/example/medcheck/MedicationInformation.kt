package com.example.medcheck

import DrugData
import DrugLabel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMainBinding
import com.example.medcheck.databinding.ActivityMedicationInformationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder
import java.util.Calendar

class MedicationInformation : AppCompatActivity() {

    // declaring the binding activity
    private lateinit var binding: ActivityMedicationInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medication_information)
        // binding
        binding = ActivityMedicationInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // calling the Ui components
        setupUI()
        // Setup SearchView listener
        setupSearchListener()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_preferences -> {
                    // Navigates to preferences
                    startActivity(Intent(this, Preferences::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_calendar -> {
                    // Navigate to Calendar Activity
                    startActivity(Intent(this, Calendar::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_dashboard -> {
                    // Navigate to Dashboard Activity
                    startActivity(Intent(this, Dashboard::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_konw_your_med -> {
                    // Navigate to About Med Activity
                    startActivity(Intent(this, MedicationInformation::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                R.id.nav_medication -> {
                    // Navigate to Medication Activity
                    startActivity(Intent(this, MyMedicine::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false
        }
//--------------------------------------------------------------------------------------------------
    }

    private fun setupUI() {
        // Inset handling for edge-to-edge layouts
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupSearchListener() {
        binding.searchMedication.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    fetchDrugInfo(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun fetchDrugInfo(searchTerm: String) {
        val apiKey = "FqQd4af2TNmewJ4k1tTtVI0LvQHhVRfg6DDhSIsE" // replace with your OpenFDA API key
        val limit = 5
        val encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8")

        val call = RetrofitClient.api.getDrugInfo(encodedSearchTerm, limit, apiKey)
        call.enqueue(object : Callback<DrugData> {
            override fun onResponse(call: Call<DrugData>, response: Response<DrugData>) {
                if (response.isSuccessful) {
                    val drugData = response.body()
                    if (!drugData?.results.isNullOrEmpty()) {
                        updateUIWithDrugData(drugData!!.results[0])
                    } else {
                        showToast("Sorry, this drug is not available in our database.")
                    }
                } else {
                    showToast("Failed to retrieve data.")
                }
            }
            override fun onFailure(call: Call<DrugData>, t: Throwable) {
                Log.e("MedicationInformation", "Error: ${t.message}")
                showToast("Error fetching data: ${t.message}")
            }
        })
    }

    private fun updateUIWithDrugData(drugLabel: DrugLabel) {
        // Display brand name
        val brandName = drugLabel.openfda.brand_name?.joinToString(", ") ?: "N/A"
        binding.tvDrugName.text = brandName

        // Display description
        val description = drugLabel.description?.joinToString("\n") ?: "No description available"
        binding.tvDescription.text = "Description: $description"

        // Display indications and usage
        val indications = drugLabel.indications_and_usage?.joinToString("\n") ?: "No indications and usage available"
        binding.tvIndications.text = "Indications and Usage: $indications"

        // Display dosage and administration
        val dosage = drugLabel.dosage_and_administration?.joinToString("\n") ?: "No dosage information available"
        binding.tvDosage.text = "Dosage: $dosage"

        // Display warnings
        val warnings = drugLabel.warnings_and_cautions?.joinToString("\n") ?: "No warnings available"
        binding.tvWarnings.text = "Warnings: $warnings"

        // Display adverse reactions
        val adverseReactions = drugLabel.adverse_reactions?.joinToString("\n") ?: "No adverse reactions available"
        binding.tvAdverseReactions.text = "Adverse Reactions: $adverseReactions"
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
