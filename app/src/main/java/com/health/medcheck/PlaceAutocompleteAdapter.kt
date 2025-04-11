package com.health.medcheck

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PlaceAutocompleteAdapter(
    context: Context,
    private val placesClient: PlacesClient
) : ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_dropdown_item_1line), Filterable {

    private var predictions = mutableListOf<AutocompletePrediction>()
    private val excludedRegions = listOf("Taiwan", "Hong Kong", "Macau")

    override fun getCount(): Int = predictions.size

    override fun getItem(position: Int): AutocompletePrediction = predictions[position]

    override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
        val view = super.getView(position, convertView, parent)
        val prediction = getItem(position)
        val textView = view.findViewById<android.widget.TextView>(android.R.id.text1)
        textView.text = prediction?.getFullText(null).toString()
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val token = AutocompleteSessionToken.newInstance()
                            val request = FindAutocompletePredictionsRequest.builder()
                                .setSessionToken(token)
                                .setQuery(constraint.toString())
                                .build()

                            val response = placesClient.findAutocompletePredictions(request).await()
                            val filteredPredictions = response.autocompletePredictions.filterNot { prediction ->
                                excludedRegions.any { region ->
                                    prediction.getFullText(null).toString().contains(region, ignoreCase = true)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                predictions = filteredPredictions.toMutableList()
                                results.values = predictions
                                results.count = predictions.size
                                publishResults(constraint, results)
                            }
                        } catch (e: Exception) {
                            Log.e("PlaceAutocompleteAdapter", "Error fetching predictions", e)
                            withContext(Dispatchers.Main) {
                                predictions = mutableListOf()
                                publishResults(constraint, results)
                            }
                        }
                    }
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results?.count ?: 0 > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
