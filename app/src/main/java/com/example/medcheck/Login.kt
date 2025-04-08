package com.example.medcheck

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medcheck.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context.CONNECTIVITY_SERVICE
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

import java.util.concurrent.Executor

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var connectivityManager: ConnectivityManager
    private var loginTask: Task<AuthResult>? = null
    private var loginAttempts = 0
    private val maxLoginAttempts = 3
    private val loginTimeout = 30000L // 30 seconds timeout
    private val fadeDuration = 300L // Animation duration in milliseconds
    private lateinit var timeoutHandler: Handler
    private var isAccountLocked = false
    private lateinit var firestore: FirebaseFirestore
    private val timeoutRunnable = Runnable {
        cancelLoginProcess()
        showErrorWithRetry("Login timed out")
    }

    companion object {
        private const val PREFS_NAME = "MedCheckPrefs"
        private const val LOGIN_ATTEMPTS_KEY = "loginAttempts"
        private const val LAST_FAILED_LOGIN_KEY = "lastFailedLogin"
        private const val COOL_DOWN_PERIOD = 300000L // 5 minutes in milliseconds
        private const val FIRST_TIME_KEY = "isFirstTime"
        private const val BIOMETRIC_ENABLED_KEY = "biometricEnabled"
        private const val TWO_FA_ENABLED_KEY = "twoFaEnabled"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable transitions before super.onCreate()
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            sharedElementsUseOverlay = false
            allowEnterTransitionOverlap = true
            allowReturnTransitionOverlap = true
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initializing firestore
        firestore = FirebaseFirestore.getInstance()
        // Postpone transition until views are ready
        postponeEnterTransition()
        binding.root.doOnPreDraw {
            startPostponedEnterTransition()
        }
        // Enable hardware acceleration
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        createNotificationChannel()

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        firebaseAuth = FirebaseAuth.getInstance()

        if (sharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, false)) {
            setupBiometricPrompt()
            biometricPrompt.authenticate(promptInfo)
        }

        // Initialize services
        firebaseAuth = FirebaseAuth.getInstance()
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        timeoutHandler = Handler(Looper.getMainLooper())
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Load login attempts from shared preferences
        loginAttempts = sharedPreferences.getInt(LOGIN_ATTEMPTS_KEY, 0)
        val lastFailedLogin = sharedPreferences.getLong(LAST_FAILED_LOGIN_KEY, 0)

        // Check if in cool down period
        if (System.currentTimeMillis() - lastFailedLogin < COOL_DOWN_PERIOD &&
            loginAttempts >= maxLoginAttempts) {
            showCoolDownMessage()
        }

        // Set up biometric authentication if enabled
        if (sharedPreferences.getBoolean(BIOMETRIC_ENABLED_KEY, false)) {
            setupBiometricPrompt()
            biometricPrompt.authenticate(promptInfo)
        }

        // submit button click listener
        binding.submitBtn.setOnClickListener {
            val email = binding.emailTxt.text.toString().trim()
            val pass = binding.passwordTxt.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                if (loginAttempts < maxLoginAttempts) {
                    if (isNetworkAvailable()) {
                        startLoginProcess(email, pass)
                    } else {
                        showError("No internet connection")
                    }
                } else {
                    showErrorWithRetry(getString(R.string.max_attempts_message))
                }
            } else {
                showError("Please fill in all fields")
            }
        }

        setupTextListeners()

        val registerLink = findViewById<TextView>(R.id.registerLink)
        registerLink.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

        binding.cancelLoginBtn.setOnClickListener {
            cancelLoginProcess()
        }

        binding.forgotPasswordBtn.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(timeoutRunnable)
        super.onDestroy()
    }

    // new function for forgot password
    private fun showForgotPasswordDialog() {
        val email = binding.emailTxt.text.toString().trim()
        val builder = AlertDialog.Builder(this)

        if (email.isNotEmpty()) {
            builder.setTitle("Reset Password")
                .setMessage("We'll send a password reset link to $email")
                .setPositiveButton("Send") { _, _ ->
                    sendPasswordResetEmail(email)
                }
                .setNegativeButton("Cancel", null)
        } else {
            val emailEditText = androidx.appcompat.widget.AppCompatEditText(this)
            emailEditText.hint = "Enter your email"

            builder.setTitle("Reset Password")
                .setMessage("Enter your email to receive a reset link")
                .setView(emailEditText)
                .setPositiveButton("Send") { _, _ ->
                    val enteredEmail = emailEditText.text.toString().trim()
                    if (enteredEmail.isNotEmpty()) {
                        sendPasswordResetEmail(enteredEmail)
                    } else {
                        Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
        }

        builder.create().show()
    }

    private fun sendPasswordResetEmail(email: String) {
        showLoading(true)
        binding.progressText.text = "Sending reset email..."

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Password reset email sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun startLoginProcess(email: String, password: String) {
        if (isAccountLocked) {
            showCoolDownMessage()
            return
        }

        // Clear previous error messages
        binding.errorMessageTxt.visibility = View.GONE
        binding.errorMessageTxt.text = ""

        showLoading(true)
        binding.progressText.text = getString(R.string.logging_in)

        // Run Firebase auth in IO dispatcher to avoid blocking UI thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if 2FA is enabled in preferences
                if (sharedPreferences.getBoolean(TWO_FA_ENABLED_KEY, false)) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        show2FADialog(email, password)
                    }
                    return@launch
                }

                // Start timeout countdown on main thread
                withContext(Dispatchers.Main) {
                    timeoutHandler.postDelayed(timeoutRunnable, loginTimeout)
                }

                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()

                withContext(Dispatchers.Main) {
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    showLoading(false)
                    resetLoginAttempts()
                    startActivity(Intent(this@Login, Dashboard::class.java))
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    showLoading(false)
                    handleLoginFailure(e, email)
                }
            }
        }
    }

    private fun show2FADialog(email: String, password: String) {
        val codeInput = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "Enter 6-digit code"
        }

        AlertDialog.Builder(this)
            .setTitle("Two-Factor Authentication")
            .setMessage("Enter the code sent to your email/phone")
            .setView(codeInput)
            .setPositiveButton("Verify") { _, _ ->
                val code = codeInput.text.toString()
                if (code.length == 6) {
                    verify2FACode(email, password, code)
                } else {
                    showError("Invalid code format")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun verify2FACode(email: String, password: String, code: String) {
        // In a real app, you would verify the code with your backend
        // For demo purposes, we'll just proceed if the code is 6 digits
        if (code.length == 6 && code.all { it.isDigit() }) {
            showLoading(true)
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    showLoading(false)
                    if (task.isSuccessful) {
                        resetLoginAttempts()
                        startActivity(Intent(this, Dashboard::class.java))
                        finish()
                    } else {
                        task.exception?.let { handleLoginFailure(it, email) }
                    }
                }
        } else {
            showError("Invalid verification code")
        }
    }

    private fun handleLoginFailure(exception: Exception, email: String) {
        loginAttempts++
        sharedPreferences.edit()
            .putInt(LOGIN_ATTEMPTS_KEY, loginAttempts)
            .putLong(LAST_FAILED_LOGIN_KEY, System.currentTimeMillis())
            .apply()

        val errorMessage = when {
            exception.message?.contains("password is invalid") == true -> {
                "Wrong password. ${maxLoginAttempts - loginAttempts} attempts remaining."
            }
            exception.message?.contains("no user record") == true -> {
                "Account not found. Please check your email."
            }
            exception.message?.contains("badly formatted") == true -> {
                "Invalid email format. Please check your email."
            }
            else -> {
                "Authentication failed: ${exception.message ?: "Unknown error"}"
            }
        }

        runOnUiThread {
            showError(errorMessage)
            highlightErrorField(errorMessage)
        }

        logLoginAttempt(email, false, errorMessage)

        if (loginAttempts >= maxLoginAttempts) {
            lockAccount()
            showCoolDownMessage()
        }
    }

    private fun lockAccount() {
        sharedPreferences.edit().putBoolean("account_locked", true).apply()
        isAccountLocked = true

        Handler(Looper.getMainLooper()).postDelayed({
            sharedPreferences.edit().putBoolean("account_locked", false).apply()
            isAccountLocked = false
            resetLoginAttempts()
        }, 15 * 60 * 1000) // 15 minutes
    }

    private fun logLoginAttempt(email: String, success: Boolean, error: String? = null) {
        val attemptData = hashMapOf(
            "email" to email,
            "timestamp" to FieldValue.serverTimestamp(),
            "success" to success,
            "error" to error,
            "deviceInfo" to Build.MODEL
        )

        firestore.collection("login_attempts")
            .add(attemptData)
            .addOnFailureListener { e ->
                Log.e("LoginAttempt", "Error logging attempt", e)
            }
    }

    private fun showError(message: String) {
        binding.errorMessageTxt.text = message
        binding.errorMessageTxt.visibility = View.VISIBLE
    }

    private fun highlightErrorField(errorMessage: String) {
        when {
            errorMessage.contains("email") -> {
                binding.emailInputLayout.error = "Invalid email"
                binding.emailInputLayout.requestFocus()
            }
            errorMessage.contains("password") -> {
                binding.passwordInputLayout.error = "Wrong password"
                binding.passwordInputLayout.requestFocus()
            }
        }
    }

    private fun setupTextListeners() {
        binding.emailTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.emailInputLayout.error = null
                binding.errorMessageTxt.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.passwordTxt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.passwordInputLayout.error = null
                binding.errorMessageTxt.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun cancelLoginProcess() {
        loginTask?.let {
            if (!it.isComplete) {
                it.addOnCanceledListener {
                    showError("Login cancelled")
                }
            }
        }
        loginTask = null
        showLoading(false)
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            // Redirect to Dashboard if already signed in
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }
    }

    // Add to a Kotlin extensions file
    fun View.doOnPreDraw(callback: () -> Unit) {
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                callback()
                return true
            }
        })
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            disableForm()
            binding.loadingOverlay.visibility = View.VISIBLE
            binding.loadingContainer.visibility = View.VISIBLE

            // Use simpler animations
            binding.loadingOverlay.alpha = 1f
            binding.loadingContainer.alpha = 1f
        } else {
            enableForm()

            binding.loadingOverlay.animate()
                .alpha(0f)
                .setDuration(150) // Shorter duration
                .withEndAction {
                    binding.loadingOverlay.visibility = View.GONE
                }
                .start()

            binding.loadingContainer.animate()
                .alpha(0f)
                .setDuration(150) // Shorter duration
                .withEndAction {
                    binding.loadingContainer.visibility = View.GONE
                }
                .start()
        }
    }

    private fun disableForm() {
        binding.emailTxt.isEnabled = false
        binding.passwordTxt.isEnabled = false
        binding.submitBtn.isEnabled = false
        binding.backBtn.isEnabled = false
    }

    private fun enableForm() {
        binding.emailTxt.isEnabled = true
        binding.passwordTxt.isEnabled = true
        binding.submitBtn.isEnabled = true
        binding.backBtn.isEnabled = true
    }

    private fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun showErrorWithRetry(message: String) {
        binding.progressText.text = message
        binding.cancelLoginBtn.text = getString(R.string.retry)
        binding.cancelLoginBtn.setOnClickListener {
            resetLoginUi()
            enableForm()
        }
    }

    private fun showCoolDownMessage() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastFailedLogin = prefs.getLong(LAST_FAILED_LOGIN_KEY, 0)
        val remainingTime = (COOL_DOWN_PERIOD - (System.currentTimeMillis() - lastFailedLogin)) / 1000

        showErrorWithRetry(getString(R.string.error_too_many_attempts, remainingTime))
        disableForm()

        // Enable form after cool down period
        Handler(Looper.getMainLooper()).postDelayed({
            resetLoginAttempts()
            enableForm()
            resetLoginUi()
        }, COOL_DOWN_PERIOD)
    }

    private fun resetLoginAttempts() {
        loginAttempts = 0
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            .putInt(LOGIN_ATTEMPTS_KEY, 0)
            .remove(LAST_FAILED_LOGIN_KEY)
            .apply()
    }

    private fun resetLoginUi() {
        binding.progressText.text = getString(R.string.logging_in)
        binding.cancelLoginBtn.text = getString(R.string.cancel)
        binding.cancelLoginBtn.setOnClickListener { cancelLoginProcess() }
    }

    private fun setupBiometricPrompt() {
        val executor: Executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                navigateToMainApp()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Use Password")
            .build()
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medication Reminders"
            val descriptionText = "Channel for medication reminder notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("medication_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}