buildscript {
    repositories {
        google()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.1") // replace with the latest version
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
}

// Rest of your build.gradle.kts content...