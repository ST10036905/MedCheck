package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityDashboardBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Dashboard : AppCompatActivity() {

	// Firebase authentication and database references
	private lateinit var auth: FirebaseAuth
	private lateinit var databaseReference: DatabaseReference
	private lateinit var binding: ActivityDashboardBinding
	// UI elements for displaying user info
	private lateinit var emailTextView: TextView
	private lateinit var medicineTextView: TextView
	private lateinit var adView: AdView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityDashboardBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Initialize AdMob
		MobileAds.initialize(this) {}
		adView = binding.adView
		loadAd()

		setupUI()
		setupFirebase()
		setupBottomNavigation()
		setupClickListeners()

		// resizes the nav bar icons depending of the screen size of the phone or device used
		val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
		bottomNavView.itemIconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
		// Initialize Firebase Auth to handle user authentication

		auth = FirebaseAuth.getInstance()
		databaseReference = FirebaseDatabase.getInstance().getReference("users")

		emailTextView = findViewById(R.id.emailText)
		medicineTextView = findViewById(R.id.medicineText)

		fetchUserData()

		// Button to navigate to AddMedicine activity
		binding.addMedicationBtn.setOnClickListener {
			val intent = Intent(this, AddMedicine::class.java)
			startActivity(intent)
		}

		// Button to navigate to MedicationInformation activity
		binding.knowMoreBtn.setOnClickListener {
			val intent = Intent(this, MedicationInformation::class.java)
			startActivity(intent)
		}

		// Button to navigate to GoogleMap activity for refilling medications
		binding.refillBtn.setOnClickListener {
			val intent = Intent(this, GoogleMap::class.java)
			startActivity(intent)
		}

	}

	private fun setupUI() {
		emailTextView = binding.emailText
		medicineTextView = binding.medicineText

		// Resize nav bar icons
		val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
		bottomNavView.itemIconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
	}

	private fun setupClickListeners() {
		binding.addMedicationBtn.setOnClickListener {
			startActivity(Intent(this, AddMedicine::class.java))
		}

		binding.knowMoreBtn.setOnClickListener {
			startActivity(Intent(this, MedicationInformation::class.java))
		}

		binding.refillBtn.setOnClickListener {
			startActivity(Intent(this, GoogleMap::class.java))
		}
	}

	private suspend fun fetchLatestMedicine(userId: String): String? {
		return try {
			val snapshot = FirebaseDatabase.getInstance()
				.getReference("medicines")
				.orderByChild("userId")
				.equalTo(userId)
				.get()
				.await()

			if (snapshot.exists()) {
				snapshot.children.lastOrNull()?.child("name")?.getValue(String::class.java)
			} else {
				null
			}
		} catch (e: Exception) {
			Log.e("Dashboard", "Error fetching medicine", e)
			null
		}
	}

	override fun onResume() {
		super.onResume()
		adView.resume()
	}

	override fun onPause() {
		adView.pause()
		super.onPause()
	}

	override fun onDestroy() {
		adView.destroy()
		super.onDestroy()
	}

	private fun setupFirebase() {
		auth = FirebaseAuth.getInstance()
		databaseReference = FirebaseDatabase.getInstance().getReference("users")
	}

	private fun loadAd() {
		val adRequest = AdRequest.Builder().build()
		adView.loadAd(adRequest)
	}

	private fun setupBottomNavigation() {
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
		bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
			when (item.itemId) {
				R.id.nav_preferences -> {
					startActivity(Intent(this, Preferences::class.java))
					true
				}
				R.id.nav_calendar -> {
					startActivity(Intent(this, Calendar::class.java))
					true
				}
				R.id.nav_konw_your_med -> {
					startActivity(Intent(this, MedicationInformation::class.java))
					true
				}
				R.id.nav_medication -> {
					startActivity(Intent(this, MyMedicine::class.java))
					true
				}
				else -> false
			}
		}
	}

	// Fetches the user's data from Firebase, including their email and last medicine.
	private fun fetchUserData() {
		val currentUser = auth.currentUser
		if (currentUser != null) {
			// Set email in emailTextView
			emailTextView.text = currentUser.email

			// Get the user ID
			val userId = currentUser.uid
			Log.d("Dashboard", "Current User ID: $userId")

			// Reference to the medicines table
			val medicinesRef = FirebaseDatabase.getInstance().getReference("medicines")

			// Query to fetch medicines where userId matches current user's ID
			medicinesRef.orderByChild("userId").equalTo(userId)
				.addListenerForSingleValueEvent(object : ValueEventListener {
					override fun onDataChange(snapshot: DataSnapshot) {
						Log.d("Dashboard", "Snapshot: ${snapshot.value}")
						if (snapshot.exists()) {
							var latestMedicineName: String? = null
							for (medicineSnapshot in snapshot.children) {
								// Fetch each medicine's name
								val name = medicineSnapshot.child("name").getValue(String::class.java)
								if (name != null) {
									latestMedicineName = name  // Store each name, resulting in the last one being saved
									Log.d("Dashboard", "Found Medicine: $name")
								}
							}

							// Display the latest (last retrieved) medicine name
							if (latestMedicineName != null) {
								medicineTextView.text = "Recent Medicine: $latestMedicineName"
							} else {
								medicineTextView.text = "No medicine found"
							}
						} else {
							medicineTextView.text = "No medicine data found"
						}
					}

					override fun onCancelled(error: DatabaseError) {
						Toast.makeText(this@Dashboard, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
					}
				})
		} else {
			Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
		}
	}
}
