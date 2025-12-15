buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // SafeArgs Plugini'nin Classpath'ini manuel olarak ekliyoruz
        // Bu, navigasyon argümanlarının (Directions) hatasız üretilmesini sağlar.
        val nav_version = "2.7.7"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")

        // Google Services
        classpath("com.google.gms:google-services:4.4.2")
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false;
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("androidx.navigation.safeargs") version "2.7.0" apply false

}