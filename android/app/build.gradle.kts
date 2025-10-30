import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization")
}

android {
    namespace = "com.dag.mypayandroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dag.mypayandroid"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.navigation.compose)

    // Metamask
    implementation(libs.metamask.android.sdk)


    // Hilt
    implementation(libs.androidx.hilt.navigation.fragment)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    androidTestImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler)
    kaptTest(libs.hilt.android.compiler)

    //web3j
    implementation(libs.core)
    implementation(libs.sol4k)
    implementation("androidx.biometric:biometric:1.4.0-alpha03")
    implementation("com.solanamobile:web3-solana:0.2.5")
    implementation("com.solanamobile:rpc-core:0.2.7")

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)

    //EncryptedSharedPreferences
    implementation(libs.androidx.security.crypto)
    //Datastore
    implementation(libs.androidx.datastore.preferences)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    implementation(libs.androidx.multidex)

}