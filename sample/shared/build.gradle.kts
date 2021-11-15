import org.jetbrains.kotlin.gradle.plugin.mpp.Framework

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

val abcLocationLib = "com.linecorp.abc:kmm-location:0.2.4"

version = "1.0"

kotlin {
    ios {
        binaries
            .filterIsInstance<Framework>()
            .forEach {
                it.transitiveExport = true
                it.export(abcLocationLib)
            }
    }
    android()

    cocoapods {
        ios.deploymentTarget = "10.0"
        homepage = "https://github.com/line/abc-kmm-location/sample/iosApp"
        summary = "Location Service Manager for Kotlin Multiplatform Mobile"
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(abcLocationLib)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                api(abcLocationLib)
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
                implementation("androidx.test:core:1.0.0")
                implementation("androidx.test:runner:1.1.0")
                implementation("org.robolectric:robolectric:4.2")
            }
        }
        val iosMain by getting {
            dependencies {
                api(abcLocationLib)
            }
        }
        val iosTest by getting
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDir("src/androidMain/res")
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}