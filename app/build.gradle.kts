plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    id ("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.basit.aitattoomaker"
    compileSdk = 35

    defaultConfig {
        applicationId = "tattoome.ai.tattoogenerator"
        minSdk = 29
        targetSdk = 35
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
    kapt {
        correctErrorTypes = true
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation (project(":stickerlibrary"))
    //multidex
    implementation(libs.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
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
    implementation(libs.ssp.android)
    implementation(libs.sdp.android)
    // ViewBinding
    implementation (libs.androidx.viewbinding)
    // Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation ("com.google.firebase:firebase-analytics-ktx")
    implementation ("com.google.firebase:firebase-crashlytics-ktx")
    implementation ("com.google.firebase:firebase-config-ktx")
    implementation ("com.google.firebase:firebase-messaging-ktx")
    // Gson & Retrofit
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    //Chucker for API
    //chuck library
    debugImplementation ("com.github.chuckerteam.chucker:library:3.5.2")
    releaseImplementation ("com.github.chuckerteam.chucker:library-no-op:3.5.2")
    //DataStore
    //datastore
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("androidx.datastore:datastore-preferences-core:1.1.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    //Hilt
    // Dagger - Hilt
    implementation ("com.google.dagger:hilt-android:2.57.1")
    kapt ("com.google.dagger:hilt-android-compiler:2.57.1")

    //work Manager
    implementation ("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.5.0")
    implementation("androidx.work:work-runtime-ktx:2.7.0")
    //Ads
    implementation("com.google.android.gms:play-services-ads:24.3.0")
    //Singular
    implementation("com.singular.sdk:singular_sdk:12.5.5")
    //Splash
    implementation("androidx.core:core-splashscreen:1.0.1")
    //Billing
    //InAppBilling
    implementation("com.android.billingclient:billing-ktx:7.1.1")
}