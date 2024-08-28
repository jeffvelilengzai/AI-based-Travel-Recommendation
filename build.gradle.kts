buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url =uri("https://jitpack.io") }
        maven { url =uri("https://chaquo.com/maven") }
    }
    dependencies {
        classpath("com.google.gms:google-services:4.3.3")
        classpath ("com.android.tools.build:gradle:7.0.2")
        classpath ("com.chaquo.python:gradle:15.0.1")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.chaquo.python") version "15.0.1" apply false
}
