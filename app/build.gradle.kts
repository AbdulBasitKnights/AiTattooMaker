plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.basit.aitattoomaker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.basit.aitattoomaker"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.1"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation (project(":stickerlibrary"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
//    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // ML Kit Selfie Segmentation
    implementation(libs.segmentation.selfie)
    implementation(libs.play.services.mlkit.subject.segmentation)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Glide for image loading
    implementation(libs.glide)
    kapt (libs.compiler)
    //ssp & sdp
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    // Hilt
//    implementation (libs.hilt.android)
//    kapt (libs.hilt.compiler)
//Wait Dialogue
//    implementation(libs.kprogresshud)
    // ViewBinding
    implementation (libs.androidx.viewbinding)
}