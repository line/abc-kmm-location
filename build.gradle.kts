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
    id("signing")
    kotlin("multiplatform")
    kotlin("native.cocoapods")
}

val isSnapshotUpload = false
group = "com.linecorp.abc"
version = "0.2.3"

kotlin {
    ios()
    android {
        publishAllLibraryVariants()
    }
    sourceSets {
        val commonMain by getting
        val androidMain by getting {
            dependencies {
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
            create<MavenPublication>("kmmLocation") {
                if (isSnapshotUpload) {
                    from(components.findByName("debug"))
                } else {
                    from(components.findByName("release"))
                }

                groupId = project.group.toString()
                artifactId = project.name
                version = if (isSnapshotUpload) "${project.version}-SNAPSHOT" else project.version.toString()
                val gitRepositoryName = "abc-$artifactId"

                pom {
                    name.set(artifactId)
                    description.set("Location Service Manager for Kotlin Multiplatform Mobile iOS and android")
                    url.set("https://github.com/line/$gitRepositoryName")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            name.set("LINE Corporation")
                            email.set("dl_oss_dev@linecorp.com")
                            url.set("https://engineering.linecorp.com/en/")
                        }
                    }

                    scm {
                        connection.set("scm:git@github.com:line/$gitRepositoryName.git")
                        developerConnection.set("scm:git:ssh://github.com:line/$gitRepositoryName.git")
                        url.set("http://github.com/line/$gitRepositoryName")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "MavenCentral"
                url = if (isSnapshotUpload) {
                    uri("https://oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                }

                val sonatypeUsername: String? by project
                val sonatypePassword: String? by project

                println("sonatypeUsername, sonatypePassword -> $sonatypeUsername, ${(sonatypePassword ?: "").map { "*" }.joinToString("")}")

                credentials {
                    username = sonatypeUsername ?: ""
                    password = sonatypePassword ?: ""
                }
            }
        }
    }
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project

        println("signingKey, signingPassword -> $signingKey, ${(signingPassword ?: "").map { "*" }.joinToString("")}")

        isRequired = !isSnapshotUpload
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["kmmLocation"])
    }
}