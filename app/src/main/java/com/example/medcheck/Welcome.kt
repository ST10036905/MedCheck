package com.example.medcheck

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityMainBinding
import com.example.medcheck.databinding.ActivityWelcomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Welcome : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val PREFS_NAME = "MedCheckPrefs"
        private const val KEY_FIRST_TIME_LOGIN = "firstTimeLogin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize view binding
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        // Set the content view to the root view of the binding
        setContentView(binding.root)

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Automatically redirect logged-in users
        if (firebaseAuth.currentUser != null) {
            checkFirstTimeLoginAndRedirect()
        }

        // Set up button listeners
        binding.createAccBtn.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        // Get the "Back" button and set up click listener
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            finish()
        }
    }

    // Check if this is the user's first login and redirect accordingly
    private fun checkFirstTimeLoginAndRedirect() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean(KEY_FIRST_TIME_LOGIN, true)

        val intent = if (isFirstTime) {
            Intent(this, AddMedicine::class.java)
        } else {
            Intent(this, Dashboard::class.java)
        }

        startActivity(intent)
        finish()
    }


    /**
    // Google Sign-In intent
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Handle result from Google Sign-In intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Authenticate with Firebase using the Google ID token
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if this is the user's first login
                    val isFirstTime = isFirstTimeLogin()
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    // Navigate based on whether it's the first login
                    if (isFirstTime) {
                        startActivity(Intent(this, AddMedicine::class.java))
                        setFirstTimeLogin(false)
                    } else {
                        startActivity(Intent(this, Dashboard::class.java))
                    }
                    finish() // Close Welcome activity
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Check if this is the user's first login
    private fun isFirstTimeLogin(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(KEY_FIRST_TIME_LOGIN, true) // Default to true if not found
    }

    // Set first-time login status
    private fun setFirstTimeLogin(isFirstTime: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(KEY_FIRST_TIME_LOGIN, isFirstTime)
            apply()
        }
    }
    **/
}