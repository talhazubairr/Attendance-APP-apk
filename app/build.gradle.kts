plugins {
    // Core Android Plugin
    id("com.android.application")

    // Google Services (Crucial for Firebase)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.attendanceapp"
    compileSdk = 34 // Using stable SDK 34 (Android 14)

    defaultConfig {
        applicationId = "com.example.attendanceapp"
        minSdk = 24
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
}

dependencies {
    // --- Standard Android UI Libraries ---
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- FIREBASE SETUP (The BoM manages versions for you) ---
    // 1. Import the BoM
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))

    // 2. Add the products you need (No version numbers needed here)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")      // Required for Login
    implementation("com.google.firebase:firebase-firestore") // Required for Database

    // --- Testing Libraries ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}