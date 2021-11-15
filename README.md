![abc-kmm-location: Location Service Manager for Kotlin Multiplatform Mobile iOS and android](images/cover.png)

[![Kotlin](https://img.shields.io/badge/kotlin-1.5.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![KMM](https://img.shields.io/badge/KMM-0.2.7-lightgreen.svg?logo=KMM)](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile)
[![AGP](https://img.shields.io/badge/AGP-7.0.1-green.svg?logo=AGP)](https://developer.android.com/studio/releases/gradle-plugin)
[![Gradle](https://img.shields.io/badge/Gradle-7.0.2-blue.svg?logo=Gradle)](https://gradle.org)
[![Platform](https://img.shields.io/badge/platform-ios,android-lightgray.svg?style=flat)](https://img.shields.io/badge/platform-ios-lightgray.svg?style=flat)

Location Service Manager for Kotlin Multiplatform Mobile iOS and android

## Features
- Super easy to use location capability in one interface
- Provides simple permission settings and management
- Dramatically reduce code to write
- Common interface available on KMM Shared

## Requirements
- iOS
  - Deployment Target 10.0 or higher
- Android
  - minSdkVersion 21

## Installation

### Gradle Settings
Add below gradle settings into your KMP (Kotlin Multiplatform Project)

#### build.gradle.kts in shared

```kotlin
plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

val abcLocationLib = "com.linecorp.abc:kmm-location:0.2.4"

kotlin {
    android()
    ios {
        binaries
            .filterIsInstance<Framework>()
            .forEach {
                it.baseName = "shared"
                it.transitiveExport = true
                it.export(abcLocationLib)
            }
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
                implementation("com.google.android.material:material:1.2.1")
                implementation(abcLocationLib)
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
                implementation(abcLocationLib)
            }
        }
        val iosTest by getting
    }
}
```
### Project Settings
Android
- You can use right now without other settings.

iOS
1. Create `Podfile` with below setting in your project root.
```bash
use_frameworks!

platform :ios, '10.0'

install! 'cocoapods', :deterministic_uuids => false

target 'iosApp' do
    pod 'shared', :path => '../shared/'
end
```
2. Run command `pod install` on the terminal

## Usage

### Register handlers for permissions

- Android

    ```kotlin
    ABCLocation
        .onPermissionUpdated(this) { isGranted ->
            println("onPermissionUpdated -> isGranted: $isGranted")
        }
        .onLocationUnavailable(this) {
            println("onLocationUnavailable")
        }
    ```

- iOS

    ```swift
    ABCLocation.Companion()
        .onPermissionUpdated(target: self) { isGranted ->
            print("onPermissionUpdated -> isGranted: \(isGranted)")
        }
        .onLocationUnavailable(target: self) {
            print("onLocationUnavailable")
        }
        .onAlwaysAllowsPermissionRequired(target: self) {
            print("onAlwaysAllowsPermissionRequired")
        }
    ```

### To get current location just once

- Android

    ```kotlin
    ABCLocation.currentLocation { data ->
        println("currentLocation -> data: $data")
    }
    ```

- iOS

    ```swift
    ABCLocation.Companion().currentLocation { data in
        print("currentLocation -> data: \(data)")
    }
    ```

### To get current location whenever location changes

- Android

    ```kotlin
    ABCLocation
        .onLocationUpdated(this) { data ->
            println("onLocationUpdated -> data: $data")
        }
        .startLocationUpdating()
    ```

- iOS

    ```swift
    ABCLocation.Companion()
        .onLocationUpdated(target: self) { data in
            print("onLocationUpdated -> data: \(data)")
        }
        .startLocationUpdating()
    ```

### To stop location updating

- Android

    ```kotlin
    ABCLocation.stopLocationUpdating()
    ```

- iOS

    ```swift
    ABCLocation.Companion().stopLocationUpdating()
    ```
