import org.jetbrains.kotlin.cli.common.toBooleanLenient

buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        val agpVersion: String by project
        val kotlinVersion: String by project
        classpath("com.android.tools.build:gradle:$agpVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

allprojects {
    ext {
        set("compileSdkVersion", 30)
        set("minSdkVersion", 21)
        set("targetSdkVersion", 30)
    }

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    id("com.android.library")
    id("maven-publish")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

val libVersion = "0.2.3"
val libVersionCode = 1
val isSnapshotUpload = false

group = "com.linecorp"
version = libVersion

kotlin {
    ios()
    android {
        publishAllLibraryVariants()
    }
    sourceSets {
        val commonMain by getting
        val androidMain by getting {
            dependencies {
                implementation("androidx.appcompat:appcompat:1.2.0")
                implementation("com.google.android.gms:play-services-location:18.0.0")
                implementation("androidx.startup:startup-runtime:1.0.0")
            }
        }
        val androidAndroidTestRelease by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13")
                implementation("androidx.test:core:1.0.0")
                implementation("androidx.test:runner:1.1.0")
                implementation("org.mockito.kotlin:mockito-kotlin:2.2.10")
                implementation("org.robolectric:robolectric:4.5.1")
            }
        }
        val androidTest by getting {
            dependsOn(androidAndroidTestRelease)
        }
        val iosMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:0.16.3")
            }
        }
        val iosTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

android {
    val compileSdkVersion = project.ext.get("compileSdkVersion") as Int
    val minSdkVersion = project.ext.get("minSdkVersion") as Int
    val targetSdkVersion = project.ext.get("targetSdkVersion") as Int

    compileSdk = compileSdkVersion
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = minSdkVersion
        targetSdk = targetSdkVersion
        println("##teamcity[setParameter name='postbuild.version' value='${libVersion}']")
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
}

val isMavenLocal = System.getProperty("maven.local").toBooleanLenient() ?: false
if (!isMavenLocal) {
    publishing {
        publications {
            create<MavenPublication>("NaverRepo") {
                if (isSnapshotUpload) {
                    from(components.findByName("debug"))
                } else {
                    from(components.findByName("release"))
                }

                groupId = project.group.toString()
                artifactId = artifactId
                version = if (isSnapshotUpload) "$libVersion-SNAPSHOT" else libVersion

                pom {
                    name.set("$groupId:$artifactId")
                    url.set("https://github.com/line/${project.name}")
                    description.set("Shared Location Manager Kotlin Multiplatform")

                    developers {
                        developer {
                            id.set("sjin.han")
                            name.set("sjin-han")
                            email.set("sjin.han@linecorp.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:ssh://github.com/line/${project.name}.git")
                        developerConnection.set("scm:git:ssh://github.com/line/${project.name}.git")
                        url.set("http://github.com/line/${project.name}")
                    }
                }
            }
        }
        repositories {
            maven {
                url = if (isSnapshotUpload) {
                    uri("http://repo.navercorp.com/m2-snapshot-repository")
                } else {
                    uri("http://repo.navercorp.com/maven2")
                }
                isAllowInsecureProtocol = true

                credentials {
                    username = System.getProperty("maven.username") ?: ""
                    password = System.getProperty("maven.password") ?: ""
                }
            }
        }
    }
}