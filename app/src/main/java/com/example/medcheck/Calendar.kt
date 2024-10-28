package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityCalendarBinding
import java.util.Calendar
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView

class Calendar : AppCompatActivity() {

	// Declare the binding variable for ActivityCalendarBinding to access views via ViewBinding
	private lateinit var binding: ActivityCalendarBinding

	// onCreate method - called when the activity is first created
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Enable edge-to-edge display for immersive experience
		enableEdgeToEdge()

		// Set content view to the XML layout file for this activity
		setContentView(R.layout.activity_calendar)

		// Adjust padding for system bars (status bar, navigation bar) for a better user experience
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets // Return the insets for further handling
		}

		// Set up ViewBinding to inflate layout and access the views
		binding = ActivityCalendarBinding.inflate(layoutInflater)
		setContentView(binding.root)

		// Getting today's date using the Calendar class
		val todayCalendar = Calendar.getInstance()
		val todayYear = todayCalendar.get(Calendar.YEAR)  // Current year
		val todayMonth = todayCalendar.get(Calendar.MONTH) // Current month (0-based)
		val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH) // Current day of the month

		// Set a listener for date selection on the CalendarView
		binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
			// Check if the selected date matches today's date
			if (year == todayYear && month == todayMonth && dayOfMonth == todayDay) {
				// If the selected date is today, navigate to the Dashboard activity
				val TakenMedsIntent = Intent(this, Dashboard::class.java)
				startActivity(TakenMedsIntent)
			} else {
				// Show a Toast message if the selected date is not today's date
				Toast.makeText(this, "Please Pick Today's Date", Toast.LENGTH_SHORT).show()
			}
		}

		//---------------------------------------BOTTOM NAVIGATION SETUP-------------------------------------------------

		// Initialize the BottomNavigationView
		val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

		// Set a listener for handling bottom navigation item clicks
		bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
			when (item.itemId) {
				// Navigate to the Preferences screen when preferences item is selected
				R.id.nav_preferences -> {
					startActivity(Intent(this, Preferences::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				// Stay on the current Calendar activity when calendar item is selected
				R.id.nav_calendar -> {
					startActivity(Intent(this, Calendar::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				// Navigate to the Dashboard screen when dashboard item is selected
				R.id.nav_dashboard -> {
					startActivity(Intent(this, Dashboard::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				// Navigate to the Medication Information screen when medication info item is selected
				R.id.nav_konw_your_med -> {
					startActivity(Intent(this, MedicationInformation::class.java))
					return@setOnNavigationItemSelectedListener true
				}

				// Navigate to the MyMedicine screen when medication item is selected
				R.id.nav_medication -> {
					startActivity(Intent(this, MyMedicine::class.java))
					return@setOnNavigationItemSelectedListener true
				}
			}
			false // Return false to let the system handle any other actions
		}
	}
}
