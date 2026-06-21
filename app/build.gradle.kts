import java.util.Properties

plugins {
    id("com.android.application")
    alias(libs.plugins.google.gms.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        localProperties.load(stream)
    }
}

val geminiApiKeys: String = localProperties.getProperty("GEMINI_API_KEYS") ?: "MISSING_KEY"
val groqApiKey: String = localProperties.getProperty("GROQ_API_KEY") ?: "MISSING_KEY"
val calorieNinjasApiKey: String = localProperties.getProperty("CALORIE_NINJAS_API_KEY") ?: "MISSING_KEY"

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

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }

        buildConfigField("String", "GEMINI_API_KEYS", "\"$geminiApiKeys\"")
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")
        buildConfigField("String", "CALORIE_NINJAS_API_KEY", "\"$calorieNinjasApiKey\"")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

@Suppress("UseTomlInstead")
dependencies {
    // UI & Core
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Architecture & Lifecycle (MVVM)
    implementation("androidx.activity:activity:1.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.1")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // Database (Room)
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Network
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Third-Party
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.murgupluoglu:flagkit-android:1.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("androidx.work:work-runtime:2.9.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Google Services & Firebase
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.firebase.messaging)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}