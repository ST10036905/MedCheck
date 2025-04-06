package com.example.medcheck

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.medcheck.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.hbb20.CountryCodePicker
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest

class Register : AppCompatActivity() {

    // Binding for the register activity layout
    private lateinit var binding: ActivityRegisterBinding
    // Firebase Authentication instance
    private lateinit var firebaseAuth: FirebaseAuth
    // Firebase Database instance
    private lateinit var database: FirebaseDatabase
    // Google Sign-In client instance
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var placesClient: PlacesClient
    private lateinit var countryCodePicker: CountryCodePicker
    private var selectedCountry: String? = null
    private lateinit var firestore: FirebaseFirestore

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            val account = task.result
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            saveGoogleUserData(account)
                            Toast.makeText(this, "Google Sign-In successful", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        } else {
                            Toast.makeText(
                                this,
                                "Google Sign-In failed: ${authTask.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
        } else {
            Toast.makeText(
                this,
                "Google Sign-In failed: ${task.exception?.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Database
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.MAPS_KEY))
        }
        placesClient = Places.createClient(this)

        // Initialize country code picker
        countryCodePicker = CountryCodePicker(this).apply {
            setOnCountryChangeListener {
                selectedCountry = selectedCountryName
                binding.textPhone.setText("+${selectedCountryCode}")
            }
        }

        // Setup UI components
        setupUI()
        // Setup location autocomplete
        setupLocationAutocomplete()
        setupCountryPicker()
        // Terms link click listener
        binding.termsLink.setOnClickListener {
            showTermsDialog()
        }

        // Country picker button click listener
        binding.countryPickerBtn.setOnClickListener {
            countryCodePicker.launchCountrySelectionDialog()
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso) // Create the GoogleSignInClient

        // Set up click listener for Google Sign-In button
        binding.googleSignInBtn.setOnClickListener {
            signInWithGoogle() // Call the function to sign in with Google
        }

        // Set up click listener for the back button
        val backBtn = findViewById<Button>(R.id.backBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, Welcome::class.java) // Navigate back to the Welcome activity
            startActivity(intent)
        }

        // Set up click listener for the submit button
        binding.submitBtn.setOnClickListener {

            if (validateForm()) {
                registerUser()
            }

            // Retrieve user input from the form
            val email = binding.textEmail.text.toString()
            val age = binding.textAge.text.toString()
            val weight = binding.textWeight.text.toString()
            val height = binding.textHeight.text.toString()
            val pass = binding.textPass.text.toString()
            val pass2 = binding.textPass2.text.toString()

            // Check if all fields are filled
            if (email.isNotEmpty() && age.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty() && pass.isNotEmpty() && pass2.isNotEmpty()) {

                if (!PasswordStrengthChecker.meetsRequirements(pass)) {
                    Toast.makeText(this, getString(R.string.password_requirements), Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (pass == pass2) { // Ensure passwords match

                    // Create user with email and password
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = firebaseAuth.currentUser?.uid // Get the new user's ID
                                // Store user data in Firebase Database
                                val userRef = database.getReference("Users").child(userId!!)
                                val userData = mapOf("email" to email, "age" to age, "weight" to weight, "height" to height)
                                userRef.setValue(userData) // Save user data in the database

                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Navigate to the Dashboard after registration
                                val intentDashboard = Intent(this, Dashboard::class.java)
                                intentDashboard.putExtra("email", email)
                                intentDashboard.putExtra("age", age)
                                startActivity(intentDashboard)
                                finish() // Close the current activity
                            } else {
                                // Display error message if registration fails
                                Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    // Notify user if passwords do not match
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Prompt user to fill in all fields if any are empty
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        setupPasswordStrengthChecker()
    }

    private fun setupUI() {
        setupLocationAutocomplete()

        binding.termsLink.setOnClickListener {
            showTermsDialog()
        }

        binding.countryPickerBtn.setOnClickListener {
            countryCodePicker.launchCountrySelectionDialog()
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInBtn.setOnClickListener {
            signInWithGoogle()
        }

        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, Welcome::class.java))
        }

        binding.submitBtn.setOnClickListener {
            if (validateForm()) {
                registerUser()
            }
        }

        setupPasswordStrengthChecker()
    }


    private fun registerUserWithEmail() {
        val email = binding.textEmail.text.toString()
        val password = binding.textPass.text.toString()
        val name = binding.textName.text.toString()
        val phone = binding.textPhone.text.toString()
        val location = binding.textLocation.text.toString()
        val age = binding.textAge.text.toString()
        val weight = binding.textWeight.text.toString()
        val height = binding.textHeight.text.toString()

        if (!PasswordStrengthChecker.meetsRequirements(password)) {
            Toast.makeText(this, getString(R.string.password_requirements), Toast.LENGTH_LONG).show()
            return
        }

        if (password != binding.textPass2.text.toString()) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Send verification email
                    firebaseAuth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                saveUserData(name, email, phone, location, age, weight, height)
                                Toast.makeText(
                                    this,
                                    R.string.verification_email_sent,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToDashboard()
                            } else {
                                Toast.makeText(
                                    this,
                                    R.string.verification_email_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: getString(R.string.registration_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveGoogleUserData(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        val user = hashMapOf(
            "name" to account.displayName,
            "email" to account.email,
            "photoUrl" to account.photoUrl?.toString(),
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "")
            .set(user)
            .addOnSuccessListener {
                Log.d("Register", "Google user data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Register", "Error saving Google user data", e)
            }
    }


    private fun navigateToDashboard() {
        val intent = Intent(this, Dashboard::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupPasswordStrengthChecker() {
        binding.textPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    updatePasswordStrength(it.toString())
                }
            }
        })
    }

    private fun setupLocationAutocomplete() {
        val autocompleteAdapter = PlaceAutocompleteAdapter(this, placesClient)
        binding.textLocation.setAdapter(autocompleteAdapter)

        binding.textLocation.setOnItemClickListener { _, _, position, _ ->
            val prediction = autocompleteAdapter.getItem(position)
            prediction?.let {
                val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.NAME)
                val request = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)

                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    val place = response.place
                    // Prefer address, fall back to name if address not available
                    binding.textLocation.setText(place.address ?: place.name)
                }.addOnFailureListener { exception ->
                    Log.e("Register", "Place not found: ${exception.message}")
                    Toast.makeText(this, "Failed to get place details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCountryPicker() {
        binding.countryPickerBtn.setOnClickListener {
            try {
                countryCodePicker.launchCountrySelectionDialog()
            } catch (e: Exception) {
                Log.e("Register", "Error showing country picker: ${e.message}")
                Toast.makeText(this, "Could not load country picker", Toast.LENGTH_SHORT).show()
            }
        }

        // default country
        countryCodePicker.setCountryForNameCode("Angola")
    }
    private fun showTermsDialog() {
        try {
            val termsContent = getString(R.string.acceptance_of_terms) // Make sure this string exists

            AlertDialog.Builder(this)
                .setTitle(getString(R.string.terms_and_conditions))
                .setMessage(termsContent)
                .setPositiveButton(getString(R.string.agree)) { dialog, _ ->
                    binding.termsCheckbox.isChecked = true
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .apply {
                    setCanceledOnTouchOutside(false)
                    show()
                }
        } catch (e: Exception) {
            Log.e("Register", "Error showing terms dialog: ${e.message}")
            Toast.makeText(this, "Could not load terms", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateForm(): Boolean {
        if (!binding.termsCheckbox.isChecked) {
            Toast.makeText(this, R.string.accept_terms_error, Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.textLocation.text.isNullOrEmpty()) {
            binding.locationInputLayout.error = getString(R.string.location_required)
            return false
        }

        // Add other validations as needed
        return true
    }


    private fun updatePasswordStrength(password: String) {
        val strength = PasswordStrengthChecker.calculateStrength(password)

        when (strength) {
            PasswordStrengthChecker.Strength.WEAK -> {
                binding.passwordStrengthText.text = getString(R.string.weak)
                binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.weak_password))
                updateStrengthMeter(R.color.weak_password, 1)
            }
            PasswordStrengthChecker.Strength.MEDIUM -> {
                binding.passwordStrengthText.text = getString(R.string.medium)
                binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.medium_password))
                updateStrengthMeter(R.color.medium_password, 2)
            }
            PasswordStrengthChecker.Strength.STRONG -> {
                binding.passwordStrengthText.text = getString(R.string.strong)
                binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.strong_password))
                updateStrengthMeter(R.color.strong_password, 3)
            }
            PasswordStrengthChecker.Strength.VERY_STRONG -> {
                binding.passwordStrengthText.text = getString(R.string.very_strong)
                binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.very_strong_password))
                updateStrengthMeter(R.color.very_strong_password, 4)
            }
        }
    }

    private fun registerUser() {
        val email = binding.textEmail.text.toString()
        val password = binding.textPass.text.toString()
        val name = binding.textName.text.toString()
        val phone = binding.textPhone.text.toString()
        val location = binding.textLocation.text.toString()
        val age = binding.textAge.text.toString()
        val weight = binding.textWeight.text.toString()
        val height = binding.textHeight.text.toString()

        if (!PasswordStrengthChecker.meetsRequirements(password)) {
            Toast.makeText(this, getString(R.string.password_requirements), Toast.LENGTH_LONG).show()
            return
        }

        if (password != binding.textPass2.text.toString()) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseAuth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                saveUserData(name, email, phone, location, age, weight, height)
                                Toast.makeText(
                                    this,
                                    R.string.verification_email_sent,
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToDashboard()
                            } else {
                                Toast.makeText(
                                    this,
                                    R.string.verification_email_failed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message ?: getString(R.string.registration_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun saveUserData(
        name: String,
        email: String,
        phone: String,
        location: String,
        age: String,
        weight: String,
        height: String
    ) {
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "location" to location,
            "age" to age,
            "weight" to weight,
            "height" to height,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(firebaseAuth.currentUser?.uid ?: "")
            .set(user)
            .addOnSuccessListener {
                Log.d("Register", "User data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.w("Register", "Error saving user data", e)
            }
    }


    private fun updateStrengthMeter(colorRes: Int, segments: Int) {
        val color = ContextCompat.getColor(this, colorRes)

        binding.strengthMeter1.setBackgroundColor(color)
        binding.strengthMeter2.setBackgroundColor(if (segments >= 2) color else ContextCompat.getColor(this, R.color.m3_outline))
        binding.strengthMeter3.setBackgroundColor(if (segments >= 3) color else ContextCompat.getColor(this, R.color.m3_outline))
        binding.strengthMeter4.setBackgroundColor(if (segments >= 4) color else ContextCompat.getColor(this, R.color.m3_outline))
    }

    // Function to initiate Google Sign-In
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent // Get the sign-in intent
        googleSignInLauncher.launch(signInIntent) // Launch the sign-in activity
    }
}