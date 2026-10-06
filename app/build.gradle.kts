// app/build.gradle.kts

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.mgba.emulator"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mgba.emulator"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // NDK設定
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // CMakeの設定 - 一時的に無効化（デバッグ用）
        // externalNativeBuild {
        //     cmake {
        //         cppFlags.add("-std=c++17")
        //         cppFlags.add("-fexceptions")
        //         cppFlags.add("-frtti")
        //         arguments.add("-DANDROID_STL=c++_shared")
        //         arguments.add("-DMGBA_CORE_ONLY=ON")
        //     }
        // }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    // CMake設定 - 一時的に無効化（デバッグ用）
    // externalNativeBuild {
    //     cmake {
    //         path = file("src/main/jni/CMakeLists.txt")
    //         version = "3.22.1"
    //     }
    // }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Kotlin・Android基本
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // UI
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ファイルアクセス
    implementation("androidx.documentfile:documentfile:1.0.1")

    // コルーチン
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ライフサイクル
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // シリアライゼーション（セーブデータ）
    implementation("org.json:json:20231013")

    // テスト
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
