plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {

    namespace = "com.aegis.pdf"
    compileSdk = 34

    defaultConfig {

        applicationId = "com.aegis.pdf"

        minSdk = 26
        targetSdk = 34

        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {

            abiFilters += listOf(
                "arm64-v8a",
                "armeabi-v7a"
            )
        }

        externalNativeBuild {

            cmake {

                cppFlags += listOf(
                    "-std=c++17",
                    "-O3",
                    "-DNDEBUG",
                    "-ffast-math",
                    "-fvisibility=hidden",
                    "-ffunction-sections",
                    "-fdata-sections"
                )

                arguments += listOf(
                    "-DANDROID_STL=c++_shared",
                    "-DANDROID_PLATFORM=android-26"
                )
            }
        }
    }

    buildTypes {

        debug {

            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            isDebuggable = true

            externalNativeBuild {

                cmake {

                    cppFlags += listOf(
                        "-O0",
                        "-g"
                    )
                }
            }
        }

        release {

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )

            externalNativeBuild {

                cmake {

                    cppFlags += listOf(
                        "-O3",
                        "-DNDEBUG",
                        "-fvisibility=hidden"
                    )
                }
            }
        }
    }

    externalNativeBuild {

        cmake {

            path = file(
                "src/main/cpp/CMakeLists.txt"
            )

            version = "3.22.1"
        }
    }

    compileOptions {

        sourceCompatibility =
            JavaVersion.VERSION_17

        targetCompatibility =
            JavaVersion.VERSION_17
    }

    kotlinOptions {

        jvmTarget = "17"

        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    buildFeatures {

        compose = true
        buildConfig = true
    }

    composeOptions {

        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {

        resources {

            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
        }

        jniLibs {

            useLegacyPackaging = false
        }
    }
}

dependencies {

    // =========================================
    // Compose BOM
    // =========================================

    implementation(
        platform(
            "androidx.compose:compose-bom:2024.02.01"
        )
    )

    // =========================================
    // Compose UI
    // =========================================

    implementation("androidx.compose.ui:ui")

    implementation(
        "androidx.compose.material3:material3"
    )

    implementation(
        "androidx.compose.material:material-icons-extended:1.6.1"
    )

    implementation(
        "androidx.compose.ui:ui-tooling-preview"
    )

    implementation(
        "androidx.activity:activity-compose:1.8.2"
    )

    debugImplementation(
        "androidx.compose.ui:ui-tooling"
    )

    // =========================================
    // Navigation
    // =========================================

    implementation(
        "androidx.navigation:navigation-compose:2.7.7"
    )

    // =========================================
    // Hilt
    // =========================================

    implementation(
        "com.google.dagger:hilt-android:2.50"
    )

    kapt(
        "com.google.dagger:hilt-android-compiler:2.50"
    )

    implementation(
        "androidx.hilt:hilt-navigation-compose:1.1.0"
    )

    // =========================================
    // Room Database
    // =========================================

    implementation(
        "androidx.room:room-runtime:2.6.1"
    )

    kapt(
        "androidx.room:room-compiler:2.6.1"
    )

    implementation(
        "androidx.room:room-ktx:2.6.1"
    )

    implementation(
        "androidx.room:room-testing:2.6.1"
    )

    // =========================================
    // Lifecycle
    // =========================================

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-compose:2.7.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
    )

    // =========================================
    // Core Android
    // =========================================

    implementation(
        "androidx.core:core-ktx:1.12.0"
    )

    implementation(
        "androidx.appcompat:appcompat:1.6.1"
    )

    // =========================================
    // Coroutines
    // =========================================

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3"
    )

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"
    )

    // =========================================
    // CameraX
    // =========================================

    val cameraxVersion = "1.3.1"

    implementation(
        "androidx.camera:camera-core:$cameraxVersion"
    )

    implementation(
        "androidx.camera:camera-camera2:$cameraxVersion"
    )

    implementation(
        "androidx.camera:camera-lifecycle:$cameraxVersion"
    )

    implementation(
        "androidx.camera:camera-view:$cameraxVersion"
    )

    implementation(
        "androidx.camera:camera-extensions:$cameraxVersion"
    )

    // =========================================
    // OpenCV
    // =========================================

    implementation(
        "org.opencv:opencv-android:4.8.1"
    )

    // =========================================
    // OCR
    // =========================================

    implementation(
        "com.google.mlkit:text-recognition:16.0.0"
    )

    // =========================================
    // PDF Engine
    // =========================================

    implementation(
        "com.itextpdf:itext7-core:7.2.5"
    )

    // =========================================
    // PDF Viewer
    // =========================================

    implementation(
        "com.github.barteksc:android-pdf-viewer:3.2.0-beta.1"
    )

    // =========================================
    // Image Loading
    // =========================================

    implementation(
        "io.coil-kt:coil-compose:2.5.0"
    )

    // =========================================
    // EXIF
    // =========================================

    implementation(
        "androidx.exifinterface:exifinterface:1.3.6"
    )

    // =========================================
    // Startup Optimization
    // =========================================

    implementation(
        "androidx.startup:startup-runtime:1.1.1"
    )

    implementation(
        "androidx.profileinstaller:profileinstaller:1.3.1"
    )

    // =========================================
    // Native Loader
    // =========================================

    implementation(
        "com.facebook.soloader:soloader:0.10.5"
    )

    // =========================================
    // Testing
    // =========================================

    testImplementation(
        "junit:junit:4.13.2"
    )

    testImplementation(
        "com.google.truth:truth:1.1.5"
    )

    testImplementation(
        "io.mockk:mockk:1.13.8"
    )

    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
    )

    androidTestImplementation(
        "androidx.test.ext:junit:1.1.5"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.5.1"
    )

    androidTestImplementation(
        "androidx.compose.ui:ui-test-junit4"
    )
}

kapt {

    correctErrorTypes = true
}