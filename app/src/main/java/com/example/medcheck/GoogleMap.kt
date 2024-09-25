package com.example.medcheck

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.net.PlacesClient

class GoogleMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var radioGroup: RadioGroup
    // declaring the map variable
    private var map : GoogleMap? = null
    private lateinit var mapSearchView: SearchView

    // declaring the search
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_google_map)
        // finds the search view by id
        mapSearchView = findViewById(R.id.mapSearch);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mapSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Handle search query submission
                if (query != null) {
                    searchLocation(query)  // This function would implement the search logic
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle live query changes if necessary
                return false
            }
        })


        radioGroup = findViewById(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener{ _ , itemId : Int ->
            when(itemId){
               R.id.btnNormal ->{
                   map?.mapType = GoogleMap.MAP_TYPE_NORMAL
               }
                R.id.btnHybrid ->{
                    map?.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
                R.id.btnSatellite ->{
                    map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                }
                R.id.btnTerrain ->{
                    map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
                }
            }
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

        mapFragment?.getMapAsync(this)
    }

    private fun searchLocation(query: String) {

    }

    override fun onMapReady(map: GoogleMap?) {
        // setting a style for the map
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        // function to set the map type
        this.map = map
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL

        // sets the map default location
       val latLng = LatLng(-33.964314, 18.467859)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15f))

        // adding map functions
        map?.uiSettings?.isZoomControlsEnabled = true
        map?.uiSettings?.isZoomGesturesEnabled = true
        map?.uiSettings?.isCompassEnabled = true
        map?.uiSettings?.isRotateGesturesEnabled = true
        map?.uiSettings?.isTiltGesturesEnabled = true
        map?.uiSettings?.isScrollGesturesEnabled = true
        map?.uiSettings?.isScrollGesturesEnabledDuringRotateOrZoom = true
        map?.uiSettings?.isMyLocationButtonEnabled = true
        // marker options
        val markerOptions =MarkerOptions()
        markerOptions.position(latLng)
        markerOptions.title("Location")
        markerOptions.snippet("Marker location")
        markerOptions.draggable(true)
        markerOptions.flat(true)
        // adds the marker to the map location
        map?.addMarker(markerOptions)

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromDrawable(R.drawable.location)))
    }

    private fun getBitmapFromDrawable(resId: Int): Bitmap? {
        var bitmap : Bitmap? = null
        val drawable = ResourcesCompat.getDrawable(resources, resId , null)
        if (drawable != null){
            bitmap = Bitmap.createBitmap(150, 150 , Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0,canvas.width ,canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }
}