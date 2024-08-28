plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.chaquo.python")
}

android {
    namespace = "my.edu.tarc.travel1"
    compileSdk = 34

    defaultConfig {
        applicationId = "my.edu.tarc.travel1"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            // On Apple silicon, you can omit x86_64.
            abiFilters += listOf("arm64-v8a", "x86_64")
        }

        flavorDimensions += "pyVersion"
        productFlavors {
            create("py310") { dimension = "pyVersion" }
            create("py311") { dimension = "pyVersion" }
        }

//        python{
//            buildPython("C:/Users/Lenovo/AppData/Local/Microsoft/WindowsApps/python.exe")
//        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
  //  implementation ("com.chaquo.python:gradle:15.0.0")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.2")
    //implementation ("com.chaquo.python:glpk:4.65")


    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


}

chaquopy {

    defaultConfig {
      //  buildPython("C:\\Users\\Lenovo\\AppData\\Local\\Microsoft\\WindowsApps\\python.exe")
      //  buildPython("C:/path/to/python.exe")
      //  buildPython("C:/path/to/py.exe", "-3.8")
      //buildPython("C:/Users/Lenovo/AppData/Local/Microsoft/WindowsApps/python.exe", "-3.8")
        buildPython("C:/Users/Lenovo/AppData/Local/Programs/Python/Python310/python.exe")
      //  buildPython("C:/Python312/python.exe","-3.10")
        //version = "3.8"
        pip{
            install("pandas")
            install("datetime")
            install ("numpy")
            install ("scikit-learn")
            install ("PuLP")
           // install ("glpk")
        }
    }


    productFlavors {
        getByName("py310") { version = "3.10" }
        getByName("py311") { version = "3.11" }
    }
    sourceSets {
        sourceSets {
            getByName("main") {
                srcDir("src/main/python")
            }
        }
    }

}