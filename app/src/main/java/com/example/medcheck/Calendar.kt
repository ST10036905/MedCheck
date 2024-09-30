package com.example.medcheck
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityCalendarBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar


class Calendar : AppCompatActivity() {
	
	private lateinit var binding:ActivityCalendarBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContentView(R.layout.activity_calendar)
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
			insets
		}
//--------------------------------------------------------------------------------------------------
		// Use View Binding to set the layout
		binding = ActivityCalendarBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		// getting today's date
		val todayCalendar = Calendar.getInstance()
		val todayYear = todayCalendar.get(Calendar.YEAR)
		val todayMonth = todayCalendar.get(Calendar.MONTH)
		val todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
		
		// listener for when the user selects today's date  on the CalendarView
		binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
			// decision on whether the selected date is today's date
			if (year == todayYear && month == todayMonth && dayOfMonth == todayDay) {
				// Navigate to NextActivity
				val takenMedsintent = Intent(this,TakenMedication::class.java)
				startActivity(takenMedsintent )
			} else {
				// Show a Toast if the selected date is not today
				Toast.makeText(this, "Please Pick Today's Date", Toast.LENGTH_SHORT).show()
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
}