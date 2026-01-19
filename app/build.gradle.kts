plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") // Kotlin Android plugin
    id("com.google.gms.google-services") // Firebase / Google services
    id("com.google.devtools.ksp") // KSP
    id("com.google.dagger.hilt.android") // Dagger / hilt
    id("androidx.navigation.safeargs") // Safe Args
    id("kotlin-parcelize")
}

android {
    namespace = "com.settlex.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.settlex.android"
        minSdk = 27
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Firebase Dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.config)

    // Coroutines
    implementation(libs.kotlinx.coroutines.play.services)

    // Dots Indicator
    implementation(libs.dotsindicator)

    // ViewPager Indicator
    implementation(libs.viewpagerindicator)

    // Splash Screen
    implementation(libs.core.splashscreen)

    // Glide
    implementation(libs.glide)

    // Gson
    implementation(libs.gson)

    /** Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson) */

    // Lottie
    implementation(libs.lottie)

    // ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    // Shimmer Effect
    implementation(libs.shimmer)

    // Navigation Component
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)

    // DataStore (Preferences)
    implementation(libs.datastore.preferences)

    // Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Object Mapper
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.databind)

    // Currency EditText
    implementation(libs.currencyedittext)

    // PinView
    implementation(libs.pinview)

    // Biometric
    implementation(libs.biometric)

    // UCrop
    implementation(libs.ucrop)

    // Intent compact
    implementation(libs.core.ktx)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

