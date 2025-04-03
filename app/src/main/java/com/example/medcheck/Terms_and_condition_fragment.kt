package com.example.medcheck

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

// Add at the top of class
private lateinit var sharedPreferences: SharedPreferences
class Terms_and_condition_fragment : Fragment() {

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_terms_and_condition_fragment, container, false)

        sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAcceptTerms = view.findViewById<MaterialButton>(R.id.btnAcceptTerms)

        btnAcceptTerms.setOnClickListener { button ->
            // Add haptic feedback
            button.performHapticFeedback(android.view.HapticFeedbackConstants.CONTEXT_CLICK)

            // Button press animation
            button.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    // Show confirmation message
                    showAcceptanceConfirmation(view)
                }.start()
        }
    }

    private fun showAcceptanceConfirmation(view: View) {
        Snackbar.make(
            view,
            getString(R.string.terms_accepted_confirmation),
            Snackbar.LENGTH_SHORT
        ).apply {
            animationMode = Snackbar.ANIMATION_MODE_SLIDE
            setAction(getString(R.string.dismiss)) { dismiss() }
        }.show()

        sharedPreferences.edit().putBoolean("TERMS_ACCEPTED", true).apply()
    }
}