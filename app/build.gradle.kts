plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services) // For Google services like Firebase
}

android {
    namespace = "com.example.brainalyzer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.brainalyzer"
        minSdk = 24
        targetSdk = 35 // Updated to ensure compatibility with the latest features
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core:1.12.0")
    implementation("com.google.firebase:firebase-messaging:23.2.1")

    // Firebase Libraries
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    // Third-party Libraries
    implementation("com.prolificinteractive:material-calendarview:1.4.3")
    implementation("com.android.support:support-compat:25.1.1")

    // Room Database Dependencies
    implementation("androidx.room:room-runtime:2.5.1")
    annotationProcessor("androidx.room:room-compiler:2.5.1") // For Java

    // Room Kotlin Extensions (If using Kotlin)
    implementation("androidx.room:room-ktx:2.5.1")

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
