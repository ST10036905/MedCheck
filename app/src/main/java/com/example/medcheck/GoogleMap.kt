package com.example.medcheck

import android.os.Bundle
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

class GoogleMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var radioGroup: RadioGroup
    // declacring the map variable
    private var map : GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_google_map)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
    override fun onMapReady(map: GoogleMap?) {
        // function to set the map type
        this.map = map
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        // sets the map default location
       val latLng = LatLng(-33.964314, 18.467859)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,19f))
    }
}