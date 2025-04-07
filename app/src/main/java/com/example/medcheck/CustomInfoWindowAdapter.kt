package com.example.medcheck

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        val title: TextView = view.findViewById(R.id.infoWindowTitle)
        val desc: TextView = view.findViewById(R.id.infoWindowContent)

        when (val data = marker.tag) {
            is CustomInfoWindowData -> {
                // Handle custom info window data
                title.text = data.title
                desc.text = data.desc
            }
            else -> {
                // Fallback for markers without custom data
                title.text = marker.title ?: "Location"
                desc.text = marker.snippet ?: ""
            }
        }

        return view
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null // Use default window frame
    }
}