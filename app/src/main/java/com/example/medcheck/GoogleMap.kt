package com.example.medcheck

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar


class GoogleMap : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var radioGroup: RadioGroup
    // declaring the map variable
    private var map : GoogleMap? = null
    private lateinit var mapSearchView: SearchView
    // declaring the search
    private lateinit var placesClient: PlacesClient
    // val to get user location
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var mapOptionMenu: ImageButton? = null
    private var isRadioGroupVisible = true // State to track visibility


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_google_map)
        // resizes the nav bar icons depending of the screen size of the phone or device used
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavView.itemIconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
        // Initialize views
        radioGroup = findViewById(R.id.radioGroup);
        mapOptionMenu = findViewById(R.id.mapOptionMenu);

        // Set click listener on the mapOptionMenu button
        // Set click listener on the mapOptionMenu button
        mapOptionMenu?.setOnClickListener {
            // Toggle visibility of RadioGroup
            if (isRadioGroupVisible) {
                radioGroup.visibility = View.GONE // Hide RadioGroup
            } else {
                radioGroup.visibility = View.VISIBLE // Show RadioGroup
            }
            isRadioGroupVisible = !isRadioGroupVisible // Toggle state
        }

        // initializing the places API with API key
        Places.initialize(applicationContext,"AIzaSyCV5y_AsNgheuOBVZcQ8rsVts-Hv5922PA")
        placesClient = Places.createClient(this)

        // initialize the autocompleteSupport fragment for the search bar
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autoComplete_fragment)
                as AutocompleteSupportFragment

        // specify the type of place data to return
        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        )

        // set up a listener to handle the place selection
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener{
            override fun onPlaceSelected(place: Place) {
                // Respond to the places selected
                val latLng = place.latLng
                latLng?.let {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map?.addMarker(MarkerOptions().position(latLng).title(place.name))
                }
            }

            override fun onError(p0: Status) {
                // handles the error that may occour
                Toast.makeText(this@GoogleMap, "An error occurred: ",Toast.LENGTH_SHORT).show()
            }
        })

        // set up map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    override fun onMapReady(googleMap: GoogleMap) {

        this.map = googleMap
        // setting a style for the map
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        // function to set the map type
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL

        // sets the map default location
        val latLng = LatLng(-33.9590961632977, 18.46986167931326)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,2f))

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
        markerOptions.snippet("You are here.")
        markerOptions.draggable(true)
        markerOptions.flat(true)
        // adds the marker to the map location
       val marker = map?.addMarker(markerOptions)

        marker?.tag = CustomInfoWindowData("XYZ Pharmacy", getString(R.string.pharmacy_description), R.drawable.pharmacy)
        // checks the permission to get user location
        checkLocationPermission()
        markerOptions.icon(getBitmapFromDrawable(R.drawable.location)?.let {
            BitmapDescriptorFactory.fromBitmap(
                it
            )
        })
        map?.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            map?.isMyLocationEnabled = true

            // Get the current location
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))

                    // Add a marker at the user's location
                    val markerOptions = MarkerOptions().position(currentLatLng).title("Current Location")
                    map?.addMarker(markerOptions)
                } else {
                    Toast.makeText(this, "Could not retrieve location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
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