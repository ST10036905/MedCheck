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
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.Locale

class LanguageFragment : Fragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var radioGroup: RadioGroup
    private var translator: Translator? = null  // Make translator nullable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("Preferences", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_langauge_fragment, container, false)
        radioGroup = view.findViewById(R.id.languageRadioGroup)
        setupRadioButtons(view)
        return view
    }

    private fun setupRadioButtons(view: View) {
        val selectedLanguage = sharedPreferences.getString("LANGUAGE", "en")

        when (selectedLanguage) {
            "en" -> view.findViewById<RadioButton>(R.id.radioEnglish).isChecked = true
            "pt" -> view.findViewById<RadioButton>(R.id.radioPortuguese).isChecked = true
            "af" -> view.findViewById<RadioButton>(R.id.radioAfrikaans).isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioEnglish -> updateLanguage("en", TranslateLanguage.ENGLISH)
                R.id.radioPortuguese -> updateLanguage("pt", TranslateLanguage.PORTUGUESE)
                R.id.radioAfrikaans -> updateLanguage("af", TranslateLanguage.AFRIKAANS)
            }
        }
    }

    private fun updateLanguage(languageCode: String, targetLanguage: String) {
        sharedPreferences.edit().putString("LANGUAGE", languageCode).apply()

        // Close and release previous translator if necessary
        translator?.close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()

        translator = Translation.getClient(options)

        // Download the language model for offline use
        downloadLanguageModel(languageCode)
    }

    private fun downloadLanguageModel(languageCode: String) {
        val conditions = DownloadConditions.Builder().requireWifi().build()

        translator?.downloadModelIfNeeded(conditions)
            ?.addOnSuccessListener {
                Toast.makeText(context, "Language model for $languageCode downloaded", Toast.LENGTH_SHORT).show()
                updateAppLocale(languageCode)
                translateSampleText() // Translate a sample text to confirm
            }
            ?.addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to download model: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        requireContext().createConfigurationContext(config)

        parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
    }

    private fun translateText(text: String) {
        translator?.translate(text)
            ?.addOnSuccessListener { translatedText ->
                Toast.makeText(context, "Translated: $translatedText", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { exception ->
                Toast.makeText(context, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroy() {
        translator?.close()  // Only close if not null
        super.onDestroy()
    }

    private fun translateSampleText() {
        val sampleText = "Hello, how are you?"
        translateText(sampleText)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LanguageFragment().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
