package com.example.medcheck

import DrugData
import DrugLabel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.SearchView
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

    // Declaring the binding activity for the layout (ActivityMedicationInformationBinding)
    private lateinit var binding: ActivityMedicationInformationBinding

    // The onCreate method - called when the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Setting the layout for the activity
        setContentView(R.layout.activity_medication_information)

        // Initializing ViewBinding for the activity to easily access UI components
        binding = ActivityMedicationInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the UI components like handling system insets (e.g., status bar)
        setupUI()

        // Set up the SearchView listener to handle drug search queries
        setupSearchListener()

        // Handling system insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //---------------------------------------BOTTOM NAVIGATION-------------------------------------------------
        // Set up the BottomNavigationView for handling navigation between different activities
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle navigation item selection in the BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                // Navigate to the Preferences activity
                R.id.nav_preferences -> {
                    startActivity(Intent(this, Preferences::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                // Navigate to the Calendar activity
                R.id.nav_calendar -> {
                    startActivity(Intent(this, Calendar::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                // Navigate to the Dashboard activity
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                // Navigate to the Medication activity
                R.id.nav_medication -> {
                    startActivity(Intent(this, MyMedicine::class.java))
                    return@setOnNavigationItemSelectedListener true
                }
            }
            false // Return false to let the system handle other unhandled cases
        }
    }

    // Function to handle UI setup and system insets for edge-to-edge display
    private fun setupUI() {
        // Handling insets for edge-to-edge display on the root view
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Setting up the SearchView listener for when the user submits a query to search for a medication
    private fun setupSearchListener() {
        binding.searchMedication.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // Handling the event when the user submits a search query
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    // Call function to fetch drug information based on the search query
                    fetchDrugInfo(it)
                }
                return false
            }

            // Optional function to handle real-time text changes (unused in this case)
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    // Function to fetch drug information using Retrofit for making API requests to OpenFDA
    private fun fetchDrugInfo(searchTerm: String) {
        val apiKey = "FqQd4af2TNmewJ4k1tTtVI0LvQHhVRfg6DDhSIsE" // Replace with your OpenFDA API key
        val limit = 5 // Limit the results to 5
        val encodedSearchTerm = URLEncoder.encode(searchTerm, "UTF-8") // Encode the search term for the API call

        // Retrofit API call to fetch the drug information
        val call = RetrofitClient.api.getDrugInfo(encodedSearchTerm, limit, apiKey)
        call.enqueue(object : Callback<DrugData> {
            // Handle the response if the API call is successful
            override fun onResponse(call: Call<DrugData>, response: Response<DrugData>) {
                if (response.isSuccessful) {
                    val drugData = response.body()
                    // Check if the API returned any results
                    if (!drugData?.results.isNullOrEmpty()) {
                        // Update the UI with the first result
                        updateUIWithDrugData(drugData!!.results[0])
                    } else {
                        // Show a message if the drug is not available in the database
                        showToast("Sorry, this drug is not available in our database.")
                    }
                } else {
                    // Show a message if the API request failed
                    showToast("Failed to retrieve data.")
                }
            }

            // Handle the case when the API call fails
            override fun onFailure(call: Call<DrugData>, t: Throwable) {
                Log.e("MedicationInformation", "Error: ${t.message}")
                showToast("Error fetching data: ${t.message}")
            }
        })
    }

    // Function to update the UI with the drug data received from the API
    private fun updateUIWithDrugData(drugLabel: DrugLabel) {
        // Display the brand name of the drug
        val brandName = drugLabel.openfda.brand_name?.joinToString(", ") ?: "N/A"
        binding.tvDrugName.text = brandName

        // Display the description of the drug
        val description = drugLabel.description?.joinToString("\n") ?: "No description available"
        binding.tvDescription.text = "Description: $description"

        // Display indications and usage of the drug
        val indications = drugLabel.indications_and_usage?.joinToString("\n") ?: "No indications and usage available"
        binding.tvIndications.text = "Indications and Usage: $indications"

        // Display dosage and administration information
        val dosage = drugLabel.dosage_and_administration?.joinToString("\n") ?: "No dosage information available"
        binding.tvDosage.text = "Dosage: $dosage"

        // Display warnings associated with the drug
        val warnings = drugLabel.warnings_and_cautions?.joinToString("\n") ?: "No warnings available"
        binding.tvWarnings.text = "Warnings: $warnings"

        // Display adverse reactions associated with the drug
        val adverseReactions = drugLabel.adverse_reactions?.joinToString("\n") ?: "No adverse reactions available"
        binding.tvAdverseReactions.text = "Adverse Reactions: $adverseReactions"
    }

    // Function to display a Toast message to the user
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
