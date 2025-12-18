plugins {
    id("com.android.application")
    // Pastikan Anda sudah punya Version Catalog untuk ini,
    // jika merah ganti dengan: id("com.google.gms.google-services")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.fitme.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.fitme.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- BAGIAN YANG DIPERBAIKI ---
        javaCompileOptions {
            annotationProcessorOptions {
                // Sintaks yang benar untuk Kotlin DSL:
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
        // ------------------------------
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  //
        targetCompatibility = JavaVersion.VERSION_17  //
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // --- LIBRARY UTAMA ---
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Core KTX
    implementation("androidx.core:core-ktx:1.13.1")

    // --- Activity & Lifecycle ---
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.1")

    // --- Library FITME ---
    // 1. Lokasi
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // 2. DATABASE (ROOM)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // 3. Firebase
    implementation(libs.firebase.messaging)

    // 4. Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}