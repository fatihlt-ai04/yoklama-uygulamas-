plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Hilt ve Kapt (Annotation Processing) desteği şart
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.50" apply false
    kotlin("plugin.compose") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"

}

android {
    namespace = "com.levent.yokla"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.levent.yokla"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // KRİTİK: LoginActivity'deki binding hataları için bu satır şart
        viewBinding = true
    }
}

dependencies {
    // --- Core & AndroidX ---
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    // Activity entegrasyonu ve lifecycleScope desteği için
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    // --- Supabase (BOM 3.0.0) ---
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.0"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
// Supabase Storage bağımlılığı (BOM kullanıyorsan versiyon yazmana gerek yok)
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation(libs.androidx.activity)
    // --- Ktor (Supabase ile tam uyumlu 3.0.0 sürümleri) ---
    val ktor_version = "3.0.0"
    implementation("io.ktor:ktor-client-android:3.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-logging:3.0.0")

    // --- QR & Camera (ML Kit) ---
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // --- Hilt (Dependency Injection) ---
    implementation("com.google.dagger:hilt-android:2.50")
    // Kapt (Compiler) eklenmezse Hilt çalışmaz
    kapt("com.google.dagger:hilt-compiler:2.50")

    // --- Coil & Navigation ---
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // --- Compose (BOM Kullanarak Tek Versiyon Yönetimi) ---
    implementation(platform("androidx.compose:compose-bom:2024.02.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.airbnb.android:lottie:6.0.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
















}