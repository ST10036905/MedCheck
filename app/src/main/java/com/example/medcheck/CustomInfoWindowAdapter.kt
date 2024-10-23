package com.example.medcheck

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter (private val context : Context) : GoogleMap.InfoWindowAdapter{
    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        val title : TextView = view.findViewById(R.id.infoWindowTitle)
        val desc : TextView = view.findViewById(R.id.infoWindowContent)
        val image : ImageView = view.findViewById(R.id.infoWindow)


        val data = marker.tag as CustomInfoWindowData
        title.text = data.title
        desc.text = data.desc
        data.image.let { image.setImageResource(it) }
        return view
    }
}