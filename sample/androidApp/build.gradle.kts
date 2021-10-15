plugins {
    id("com.android.application")
    kotlin("android")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.google.android.gms:play-services-location:18.0.0")
}

android {
    compileSdk = 30
    defaultConfig {
        applicationId = "com.linecorp.abc.sharedlocation.android"
        minSdk = 21
        targetSdk = 30
    }
}