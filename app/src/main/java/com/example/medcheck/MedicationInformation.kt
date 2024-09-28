package com.example.medcheck

import DrugData
import DrugLabel
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMainBinding
import com.example.medcheck.databinding.ActivityMedicationInformationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URLEncoder

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
                        showToast("No results found.")
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
        binding.tvDrugName.text = drugLabel.openfda.brand_name?.joinToString(", ") ?: "N/A"
        binding.tvDescription.text = "Description: ${drugLabel.effective_time ?: "N/A"}"
        binding.tvDosage.text = "Dosage: N/A" // Replace with actual dosage data if available
        binding.tvWarnings.text = "Warnings: N/A" // Replace with actual warning data if available
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
