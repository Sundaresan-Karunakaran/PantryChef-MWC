plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.stepappv3"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.stepappv3"
        minSdk = 26
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.room.runtime)
    implementation(libs.play.services.ads)
    implementation(libs.play.services.maps)
    implementation(libs.cardview)
    implementation(libs.engage.core)
    annotationProcessor(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.guava:guava:33.2.1-android")

    // Google Generative AI SDK
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // Coroutines for async API calls
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Coil for loading the image into the ImageView efficiently
    implementation("io.coil-kt:coil:2.4.0")
}

