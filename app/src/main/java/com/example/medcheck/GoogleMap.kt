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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class GoogleMap : AppCompatActivity(), OnMapReadyCallback {

    // Declare UI elements and variables
    private lateinit var radioGroup: RadioGroup
    private var map: GoogleMap? = null // Holds the Google Map object
    private lateinit var mapSearchView: SearchView // For searching places
    private lateinit var placesClient: PlacesClient // Places API client for autocomplete
    private val LOCATION_PERMISSION_REQUEST_CODE = 1 // Constant for location permission request code
    private var mapOptionMenu: ImageButton? = null // Button to toggle map options
    private var isRadioGroupVisible = true // State to track the visibility of RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enable edge-to-edge display
        setContentView(R.layout.activity_google_map)

        // Initialize views
        radioGroup = findViewById(R.id.radioGroup)
        mapOptionMenu = findViewById(R.id.mapOptionMenu)

        // Toggle visibility of the RadioGroup for map type selection
        mapOptionMenu?.setOnClickListener {
            if (isRadioGroupVisible) {
                radioGroup.visibility = View.GONE // Hide RadioGroup
            } else {
                radioGroup.visibility = View.VISIBLE // Show RadioGroup
            }
            isRadioGroupVisible = !isRadioGroupVisible // Toggle visibility state
        }

        // Initialize the Places API with the API key
        Places.initialize(applicationContext, "API_KEY")
        placesClient = Places.createClient(this)

        // Set up the autocomplete fragment for searching places
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autoComplete_fragment)
                as AutocompleteSupportFragment

        // Specify the place fields to return
        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        )

        // Handle place selection from the search bar
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Move the map to the selected place
                val latLng = place.latLng
                latLng?.let {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map?.addMarker(MarkerOptions().position(latLng).title(place.name))
                }
            }

            override fun onError(status: Status) {
                // Handle error in place selection
                Toast.makeText(this@GoogleMap, "An error occurred: ${status}", Toast.LENGTH_SHORT).show()
            }
        })

        // Initialize the map fragment and get notified when the map is ready
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Handle window insets to adjust layout for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle RadioGroup changes for map type selection
        radioGroup.setOnCheckedChangeListener { _, itemId: Int ->
            when (itemId) {
                R.id.btnNormal -> map?.mapType = GoogleMap.MAP_TYPE_NORMAL // Normal map
                R.id.btnHybrid -> map?.mapType = GoogleMap.MAP_TYPE_HYBRID // Hybrid map
                R.id.btnSatellite -> map?.mapType = GoogleMap.MAP_TYPE_SATELLITE // Satellite map
                R.id.btnTerrain -> map?.mapType = GoogleMap.MAP_TYPE_TERRAIN // Terrain map
            }
        }

        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Handle bottom navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_preferences -> startActivity(Intent(this, Preferences::class.java)) // Navigate to Preferences
                R.id.nav_calendar -> startActivity(Intent(this, Calendar::class.java)) // Navigate to Calendar
                R.id.nav_dashboard -> startActivity(Intent(this, Dashboard::class.java)) // Navigate to Dashboard
                R.id.nav_konw_your_med -> startActivity(Intent(this, MedicationInformation::class.java)) // Navigate to Know Your Med
                R.id.nav_medication -> startActivity(Intent(this, MyMedicine::class.java)) // Navigate to Medication
            }
            false
        }
        //--------------------------------------------------------------------------------------------------
    }

    // Called when the map is ready to use
    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

        // Apply custom styling to the map
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL // Set default map type to normal

        // Set default location on the map
        val latLng = LatLng(-33.964314, 18.467859) // Default coordinates
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        // Enable various map controls
        map?.uiSettings?.isZoomControlsEnabled = true
        map?.uiSettings?.isCompassEnabled = true
        map?.uiSettings?.isRotateGesturesEnabled = true

        // Add a marker at the default location
        val markerOptions = MarkerOptions().position(latLng).title("Location").snippet("You are here.")
        map?.addMarker(markerOptions)

        // Check for location permission and enable user location if granted
        checkLocationPermission()
    }

    // Function to enable user location on the map
    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            map?.isMyLocationEnabled = true

            // Get user's current location and move the camera to it
            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))

                    // Add marker at user's current location
                    val markerOptions = MarkerOptions().position(currentLatLng).title("Current Location")
                    map?.addMarker(markerOptions)
                } else {
                    Toast.makeText(this, "Could not retrieve location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to check for location permissions
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableUserLocation() // If permission is already granted
        }
    }

    // Handle the result of the location permission request
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation() // If permission is granted
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to convert drawable to Bitmap for marker icons (optional, not currently used)
    private fun getBitmapFromDrawable(resId: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val drawable = ResourcesCompat.getDrawable(resources, resId, null)
        if (drawable != null) {
            bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }
}
