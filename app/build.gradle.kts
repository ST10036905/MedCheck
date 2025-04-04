plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.map.secret)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.medcheck"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.medcheck"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.google.maps)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.places)
    implementation(libs.retrofit)
    implementation(libs.retrofit2)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    // Firebase and Google Sign-In dependencies
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.database.ktx)
    implementation(libs.services.auth)
    // Google Sign-In
    implementation (libs.firebase.auth.ktx.v2200)
    implementation (libs.play.services.auth.v2050)
    // for finger print
    implementation(libs.fingerprint)
    implementation (libs.rounde.image)

    implementation (libs.firebase.auth)
    implementation (libs.services.auth)
    // Google Sign-In SDK
    // Dependency for translation
    implementation(libs.mlkit)
    implementation(libs.firebase.firestore.ktx)
    //implementing messaging feature
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Analytics and Messaging
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    implementation ("androidx.biometric:biometric:1.2.0-alpha05")  // latest stable version as of the last update

    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.maps:google-maps-services:2.2.0")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

}
