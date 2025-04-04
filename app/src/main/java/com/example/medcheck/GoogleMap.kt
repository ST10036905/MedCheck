package com.example.medcheck

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.R.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.ChipGroup
import java.util.Calendar


class GoogleMap : AppCompatActivity(), OnMapReadyCallback {

    // declaring the map variable
    private var map : GoogleMap? = null
    private lateinit var mapSearchView: SearchView
    // declaring the search
    private lateinit var placesClient: PlacesClient
    // val to get user location
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var mapOptionMenu: ImageButton? = null
    private var isRadioGroupVisible = true // State to track visibility
    private lateinit var mapTypeGroup: ChipGroup


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_google_map)
        // resizes the nav bar icons depending of the screen size of the phone or device used
        val bottomNavView = findViewById<BottomNavigationView>(id.bottom_navigation)
        bottomNavView.itemIconSize = resources.getDimensionPixelSize(dimen.icon_size)
        // Initialize views
        mapTypeGroup = findViewById(id.mapTypeGroup)
        mapOptionMenu = findViewById(id.mapOptionMenu);

        // Set click listener on the mapOptionMenu button
        mapOptionMenu?.setOnClickListener {
            mapTypeGroup.visibility = if (isRadioGroupVisible) View.GONE else View.VISIBLE
            isRadioGroupVisible = !isRadioGroupVisible
        }

        // Initialize Places API with the correct key
        try {
            val apiKey = resources.getString(R.string.google_maps_key) // Store key in strings.xml
            if (!Places.isInitialized()) {
                Places.initialize(applicationContext, apiKey)
            }
            placesClient = Places.createClient(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initialize Places API: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }

        // initialize the autocompleteSupport fragment for the search bar
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autoComplete_fragment)
                as AutocompleteSupportFragment

        // Customize the appearance
        autocompleteFragment.setHint("Search for places...")
        autocompleteFragment.view?.findViewById<EditText>(resources.getIdentifier(
            "places_autocomplete_search_input",
            "id",
            packageName
        ))?.let { editText ->
            editText.setTextColor(ContextCompat.getColor(this, R.color.m3_on_surface))
            editText.setHintTextColor(ContextCompat.getColor(this, R.color.m3_on_surface_variant))
            editText.background = null
        }

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        ))


        // set up a listener to handle the place selection
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latLng = place.latLng
                latLng?.let {
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    map?.addMarker(
                        MarkerOptions()
                            .position(latLng)
                            .title(place.name)
                            .snippet(place.address)
                    )
                }
            }

            override fun onError(status: Status) {
                Toast.makeText(this@GoogleMap,
                    "Error: ${status.statusMessage}",
                    Toast.LENGTH_SHORT).show()
            }
        })

        // set up map fragment
        val mapFragment = supportFragmentManager.findFragmentById(id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        mapTypeGroup = findViewById(id.mapTypeGroup)
        mapTypeGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                id.btnNormal -> map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                id.btnSatellite -> map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                id.btnHybrid -> map?.mapType = GoogleMap.MAP_TYPE_HYBRID
                id.btnTerrain -> map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }

        //---------------------------------------BOTTOM NAV-------------------------------------------------
        val bottomNavigationView = findViewById<BottomNavigationView>(id.bottom_navigation)

        // Handle navigation item selection
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                id.nav_preferences -> {
                    // Navigates to preferences
                    startActivity(Intent(this, Preferences::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                id.nav_calendar -> {
                    // Navigate to Calendar Activity
                    startActivity(Intent(this, Calendar::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                id.nav_dashboard -> {
                    // Navigate to Dashboard Activity
                    startActivity(Intent(this, Dashboard::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                id.nav_konw_your_med -> {
                    // Navigate to About Med Activity
                    startActivity(Intent(this, MedicationInformation::class.java))
                    return@setOnNavigationItemSelectedListener true
                }

                id.nav_medication -> {
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
        map?.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, raw.map_style))
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
        val defaultLocation = LatLng(-33.9590961632977, 18.46986167931326)
        val markerOptions = MarkerOptions()
            .position(defaultLocation)
            .title("Location")
            .snippet("You are here.")
            .draggable(true)
            .flat(true)
            .icon(getBitmapFromDrawable(R.drawable.location)?.let {
                BitmapDescriptorFactory.fromBitmap(it)
            })

        val marker = map?.addMarker(markerOptions)
        marker?.tag = CustomInfoWindowData(
            "XYZ Pharmacy",
            getString(R.string.pharmacy_description),
            R.drawable.pharmacy
        )

        // Set marker click listener
        map?.setOnMarkerClickListener { marker ->
            showNavigationOptions(marker.position)
            true // Return true to indicate we've handled the event
        }

        // Set info window click listener
        map?.setOnInfoWindowClickListener { marker ->
            showNavigationOptions(marker.position)
        }

        // Set adapter with null checks
        map?.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        // checks the permission to get user location
        checkLocationPermission()

    }

    private fun showNavigationOptions(destination: LatLng) {
        val options = arrayOf(
            "Get directions in this app",
            "Open in Google Maps",
            "Cancel"
        )

        AlertDialog.Builder(this)
            .setTitle("Navigate to destination")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showInAppDirections(destination)
                    1 -> openInGoogleMaps(destination)
                    // 2 is Cancel, does nothing
                }
            }
            .show()
    }

    private fun showInAppDirections(destination: LatLng) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val origin = LatLng(location.latitude, location.longitude)

                // Show loading
                Toast.makeText(this, "Calculating route...", Toast.LENGTH_SHORT).show()

                // In a real app, you would call Directions API here
                // This is a simplified version that just draws a straight line
                map?.addPolyline(
                    PolylineOptions()
                        .add(origin, destination)
                        .width(5f)
                        .color(Color.BLUE)
                )

                // Move camera to show both points
                val bounds = LatLngBounds.builder()
                    .include(origin)
                    .include(destination)
                    .build()
                map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            } else {
                Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openInGoogleMaps(destination: LatLng) {
        val uri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            // Google Maps app not installed, open in browser
            val webUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=" +
                        "${destination.latitude},${destination.longitude}"
            )
            startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            map?.isMyLocationEnabled = true

            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))

                    // Updated marker creation with tag
                    val marker = map?.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Current Location")
                            .snippet("Your current position")
                    )
                    marker?.tag = CustomInfoWindowData(
                        "Your Location",
                        "GPS: ${currentLatLng.latitude}, ${currentLatLng.longitude}",
                        R.drawable.location
                    )
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
        return try {
            val drawable = ResourcesCompat.getDrawable(resources, resId, null)
            drawable?.let {
                val bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
                bitmap
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating marker icon", Toast.LENGTH_SHORT).show()
            null
        }
    }
}