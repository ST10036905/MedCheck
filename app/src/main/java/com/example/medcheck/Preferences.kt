package com.example.medcheck

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medcheck.databinding.ActivityPreferencesBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executor

class Preferences : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "MedCheckPrefs"
        private const val BIOMETRIC_ENABLED_KEY = "biometricEnabled"
        private const val DARK_MODE_KEY = "night"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityPreferencesBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var currentUser: FirebaseUser? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_preferences)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //---------resizes the nav bar icons depending of the screen size of the phone or device used
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavView.itemIconSize = resources.getDimensionPixelSize(R.dimen.icon_size)

        // Initialize Firebase user
        currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
            // Optionally, redirect to login screen here
        } else {
            Toast.makeText(this, "Welcome, ${currentUser?.email}", Toast.LENGTH_SHORT).show()
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val isDarkModeEnabled = sharedPreferences.getBoolean(DARK_MODE_KEY, false)
        setDarkMode(isDarkModeEnabled)

        setupBiometricPrompt()

        binding.enableBiometricButton.setOnClickListener {
            setupBiometricAuthentication()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val themeSwitch = findViewById<SwitchCompat>(R.id.themeSwitch)
        themeSwitch.isChecked = isDarkModeEnabled
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
            editor.putBoolean(DARK_MODE_KEY, isChecked).apply()
        }

        val logoutMode = findViewById<RelativeLayout>(R.id.logoutRL)
        logoutMode.setOnClickListener {
            logout()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.termsAndConditionsRL.setOnClickListener {
            val termsFragment = Terms_and_condition_fragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, termsFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.FAQRL.setOnClickListener {
            val faqFragment = FaqFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, faqFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.whatsNewRL.setOnClickListener {
            val whatsNew = WhatsNewOnMedCheck()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, whatsNew)
                .addToBackStack(null)
                .commit()
        }

        binding.LangaugeRL.setOnClickListener {
            val languagePre = LanguageFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, languagePre)
                .addToBackStack(null)
                .commit()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_calendar -> {
                    startActivity(Intent(this, Calendar::class.java))
                    true
                }
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, Dashboard::class.java))
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

    private fun setDarkMode(isEnabled: Boolean) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun signOut() {
        mGoogleSignInClient!!.signOut().addOnCompleteListener(this) {
            val intent = Intent(this@Preferences, Welcome::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun logout() {
        Toast.makeText(this, "User is being logged out...", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()

        val sharedPreferences = getSharedPreferences("MedCheckPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, Welcome::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    sharedPreferences.edit().putBoolean(BIOMETRIC_ENABLED_KEY, true).apply()
                    Toast.makeText(this@Preferences, "Biometric login enabled", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@Preferences, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@Preferences, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
        )

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Enable Biometric Login")
            .setSubtitle("Use your fingerprint to enable biometric login")
            .setNegativeButtonText("Cancel")
            .build()
    }

    private fun setupBiometricAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(promptInfo)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Toast.makeText(this, "No biometric hardware available", Toast.LENGTH_SHORT).show()
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Toast.makeText(this, "Biometric hardware currently unavailable", Toast.LENGTH_SHORT).show()
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Toast.makeText(this, "Please set up fingerprint in your device settings", Toast.LENGTH_SHORT).show()
        }
    }
}
