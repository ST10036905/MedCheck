package com.health.medcheck

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.health.medcheck.databinding.ActivityPreferencesBinding
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
        private const val TWO_FA_ENABLED_KEY = "twoFaEnabled"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var binding: ActivityPreferencesBinding
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var currentUser: FirebaseUser? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applyTheme(this)
        // Initialize sharedPreferences FIRST
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        //---------resizes the nav bar icons depending of the screen size of the phone or device used
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavView.itemIconSize = resources.getDimensionPixelSize(R.dimen.icon_size)

        // Initialize Firebase user
        currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.no_user_logged_in), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.welcome_message, currentUser?.email ?: ""),
                Toast.LENGTH_SHORT
            ).show()
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        setupBiometricPrompt()
        setupSecuritySection()

        binding.biometricSwitch.setOnClickListener {
            setupBiometricAuthentication()
        }

        // Initialize switch state
        binding.themeSwitch.isChecked = ThemeUtils.isDarkMode(this)

        // Set switch listener
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemeUtils.setDarkMode(this, isChecked)
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        // Set up fragment transactions
        binding.termsAndConditions.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, Terms_and_condition_fragment())
                .addToBackStack(null)
                .commit()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.faqBtn.setOnClickListener {
            val faqFragment = FaqFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, faqFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.whatsNewBtn.setOnClickListener {
            val whatsNew = WhatsNewOnMedCheck()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main, whatsNew)
                .addToBackStack(null)
                .commit()
        }

        binding.languageBtn.setOnClickListener {
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(DARK_MODE_KEY, binding.themeSwitch.isChecked)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        binding.themeSwitch.isChecked = savedInstanceState.getBoolean(DARK_MODE_KEY, false)
    }


    // Add this to your Preferences class
    private fun setupSecuritySection() {
        // Set initial state of 2FA switch
        val twoFaEnabled = sharedPreferences.getBoolean(TWO_FA_ENABLED_KEY, false)
        binding.twoFaSwitch.isChecked = twoFaEnabled

        // Set up 2FA switch listener
        binding.twoFaSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                show2FAEnableDialog()
            } else {
                sharedPreferences.edit().putBoolean(TWO_FA_ENABLED_KEY, false).apply()
                Toast.makeText(this, "Two-factor authentication disabled", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up biometric switch
        binding.biometricSwitch.isChecked = sharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, false)
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setupBiometricAuthentication()
            } else {
                sharedPreferences.edit().putBoolean(BIOMETRIC_ENABLED_KEY, false).apply()
                Toast.makeText(this, "Biometric login disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun show2FAEnableDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Two-Factor Authentication")
            .setMessage("You'll need to verify your identity with a code sent to your email when logging in.")
            .setPositiveButton("Enable") { _, _ ->
                sharedPreferences.edit().putBoolean(TWO_FA_ENABLED_KEY, true).apply()
                Toast.makeText(this, "Two-factor authentication enabled", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.twoFaSwitch.isChecked = false
            }
            .show()
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
