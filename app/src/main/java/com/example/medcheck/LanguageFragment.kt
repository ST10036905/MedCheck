package com.example.medcheck

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import java.util.Locale

class LanguageFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var radioGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_langauge_fragment, container, false)

        // Initialize the RadioGroup and set listeners
        radioGroup = view.findViewById(R.id.languageRadioGroup)
        setupRadioButtons(view)

        return view
    }

    private fun setupRadioButtons(view: View) {
        // Set the selected language from SharedPreferences
        val selectedLanguage = sharedPreferences.getString("LANGUAGE", "en")

        when (selectedLanguage) {
            "en" -> view.findViewById<RadioButton>(R.id.radioEnglish).isChecked = true
            "pt" -> view.findViewById<RadioButton>(R.id.radioPortuguese).isChecked = true
            "af" -> view.findViewById<RadioButton>(R.id.radioAfrikaans).isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioEnglish -> updateLanguage("en")
                R.id.radioPortuguese -> updateLanguage("pt")
                R.id.radioAfrikaans -> updateLanguage("af")
            }
        }
    }

    private fun updateLanguage(languageCode: String) {
        // Save the selected language to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("LANGUAGE", languageCode)
            apply()
        }

        // Update the app's locale
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireContext().createConfigurationContext(config)

        // Recreate the activity to apply the new language
        activity?.recreate()
    }
}
