// 1. Plugins block - ESSENTIAL - Place this at the very top
plugins {
    id("com.android.application") // For an Android application module
    id("com.google.gms.google-services")
}

// 2. Android block - ESSENTIAL - Most of your config goes in here
android {
    // Optional: Define a namespace (recommended for AGP 7.0+)
    namespace = "si.uni_lj.fe.tnuv.flatypus" // Should match your applicationId

    compileSdk = 34 // Or your desired compile SDK version (e.g., 33, 35 if available)

    defaultConfig {
        applicationId = "si.uni_lj.fe.tnuv.flatypus"
        minSdk = 24
        targetSdk = 34 // Typically matches compileSdk or is close to it (35 is very new, ensure it's what you intend)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Consider adding vectorDrawables.useSupportLibrary = true if you use vector drawables
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Set to true for production releases to shrink and obfuscate code
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // You might also want to define signingConfigs for release builds here
        }
        // You can also define a debug buildType if needed, though defaults are often fine
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    buildFeatures {
        viewBinding = true
        // dataBinding = true // If you use data binding
        // compose = true // If you use Jetpack Compose
    }

    // Optional: Packaging options, lint options, etc. can go here
    // packagingOptions { ... }
    // lint { ... }

} // Closing brace for the android block

// Dependencies block - This was correctly placed relative to the android block's end
dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.preference)
    implementation(libs.cardview)

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.firebase.database)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}