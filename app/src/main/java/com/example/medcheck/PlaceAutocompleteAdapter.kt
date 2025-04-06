package com.example.medcheck

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import android.widget.Filter
import android.widget.TextView
import com.google.android.libraries.places.api.model.TypeFilter
import io.opencensus.stats.View


class PlaceAutocompleteAdapter(
    context: Context,
    private val placesClient: PlacesClient
) : ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_dropdown_item_1line),
    Filterable {

    private var results: List<AutocompletePrediction> = emptyList()

    override fun getCount(): Int = results.size
    override fun getItem(position: Int): AutocompletePrediction? = results.getOrNull(position)

    override fun getView(
        position: Int,
        convertView: android.view.View?,
        parent: ViewGroup
    ): android.view.View {
        val view = super.getView(position, convertView, parent) as TextView
        val item = getItem(position)
        view.text = item?.getFullText(null) ?: ""
        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(constraint.toString())
                        .setTypeFilter(TypeFilter.ADDRESS)
                        .build()

                    try {
                        val response = Tasks.await(placesClient.findAutocompletePredictions(request))
                        results = response.autocompletePredictions
                        filterResults.values = results
                        filterResults.count = results.size
                    } catch (e: Exception) {
                        Log.e("Register", "Error fetching predictions: ${e.message}")
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged()
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}