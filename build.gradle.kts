// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("com.google.dagger.hilt.android") version "2.58" apply false
    id("androidx.navigation.safeargs") version "2.9.6" apply false
}