plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // ✅ Google Services Plugin for Firebase
}

android {
    namespace = "com.example.brainalyzer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.brainalyzer"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_17 // 🔹 Updated to Java 17 (Recommended)
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ✅ AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-ktx:1.12.0") // 🔹 Updated Core KTX

    // ✅ Firebase BOM (Bill of Materials) - Ensures Compatibility
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // ✅ Firebase Services
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging") // 🔹 Required for Notifications
    implementation("com.google.firebase:firebase-analytics")

    // ✅ Room Database Dependencies
    implementation("androidx.room:room-runtime:2.6.0") // 🔹 Updated Room to Latest
    annotationProcessor("androidx.room:room-compiler:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")

    // ✅ Third-party Libraries
    implementation("com.prolificinteractive:material-calendarview:1.4.3")

    // ✅ Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("androidx.work:work-runtime:2.9.0")
}
