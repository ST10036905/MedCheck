package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityCalendarBinding
import java.util.Calendar
import android.widget.Toast
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
		//when the user picks today's date:
		/**
		 * when the user open's calendar
		 * they can click on the present date and be
		 * transferred to the taken TakenMedication.kt
		 */
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
				val TakenMedsintent = Intent(this,Dashboard::class.java)
				startActivity(TakenMedsintent )
			} else {
				// Show a Toast if the selected date is not today
				Toast.makeText(this, "Please Pick Today's Date", Toast.LENGTH_SHORT).show()
			}
		}
//--------------------------------------------------------------------------------------------------
		
		//for the navigation bar at the bottom:
		/**
		 * when an icon is clicked,the chosen activity is started (startActivoty) and
		 * the user is sent to their  chosen screen. For
		 * example: User clicks Today,
		 * the taken medication activity starts, showing
		 * the user the taken medication screen of today.
		 */


//--------------------------------------------------------------------------------------------------
	}
}