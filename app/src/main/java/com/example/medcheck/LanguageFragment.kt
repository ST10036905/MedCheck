package com.example.medcheck

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.snackbar.Snackbar
import java.util.Locale

class LanguageFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedLanguageCode: String = "en" // Default language

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE)

        // Restore selected language if fragment is recreated
        savedInstanceState?.getString("SELECTED_LANG")?.let {
            selectedLanguageCode = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_langauge_fragment, container, false)

        // Get current language from SharedPreferences if not restored from savedInstanceState
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences = requireContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
        }
        selectedLanguageCode = sharedPreferences.getString("LANGUAGE", "en") ?: "en"

        // Initialize UI components
        val radioEnglish = view.findViewById<MaterialRadioButton>(R.id.radioEnglish).apply {
            contentDescription = getString(R.string.english_language_option)
        }
        val radioPortuguese = view.findViewById<MaterialRadioButton>(R.id.radioPortuguese).apply {
            contentDescription = getString(R.string.portuguese_language_option)
        }
        val radioAfrikaans = view.findViewById<MaterialRadioButton>(R.id.radioAfrikaans).apply {
            contentDescription = getString(R.string.afrikaans_language_option)
        }
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveLanguage).apply {
            contentDescription = getString(R.string.save_language_settings)
        }

        // Set initial selection
        when (selectedLanguageCode) {
            "en" -> radioEnglish.isChecked = true
            "pt" -> radioPortuguese.isChecked = true
            "af" -> radioAfrikaans.isChecked = true
        }

        // Set click listeners for radio buttons
        radioEnglish.setOnClickListener {
            selectedLanguageCode = "en"
            radioEnglish.isChecked = true
            radioPortuguese.isChecked = false
            radioAfrikaans.isChecked = false
        }
        radioPortuguese.setOnClickListener {
            selectedLanguageCode = "pt"
            radioEnglish.isChecked = false
            radioPortuguese.isChecked = true
            radioAfrikaans.isChecked = false
        }
        radioAfrikaans.setOnClickListener {
            selectedLanguageCode = "af"
            radioEnglish.isChecked = false
            radioPortuguese.isChecked = false
            radioAfrikaans.isChecked = true
        }

        // Save button click listener with enhanced visual feedback
        btnSave.setOnClickListener { button ->
            // Button press animation
            button.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                button.animate().scaleX(1f).scaleY(1f).setDuration(100).start()

                // Disable button during processing
                button.isEnabled = false

                saveLanguage(selectedLanguageCode)

                // Re-enable button after delay
                button.postDelayed({ button.isEnabled = true }, 1000)
            }.start()
        }

        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SELECTED_LANG", selectedLanguageCode)
    }

    private fun saveLanguage(languageCode: String) {
        // Save to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("LANGUAGE", languageCode)
            apply()
        }

        // Update app locale
        updateLocale(languageCode)

        // Show confirmation
        showLanguageChangedMessage()
    }

    private fun updateLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = requireContext().resources
        val config = resources.configuration
        config.setLocale(locale)

        // For API 17+
        resources.updateConfiguration(config, resources.displayMetrics)

        // Recreate activity to apply changes
        activity?.recreate()
    }

    private fun showLanguageChangedMessage() {
        view?.let {
            Snackbar.make(
                it,
                getString(R.string.language_changed_successfully),
                Snackbar.LENGTH_LONG
            ).apply {
                animationMode = Snackbar.ANIMATION_MODE_SLIDE
                setAction(getString(R.string.dismiss)) { dismiss() }
            }.show()
        }
    }
}