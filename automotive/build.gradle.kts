plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.interactive"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.interactive"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 에뮬레이터(x86_64)와 실제 차·폰(arm64-v8a)용만 넣어 APK 크기를 줄인다
        ndk { abiFilters += listOf("x86_64", "arm64-v8a") }
    }

    // 서명 키는 저장소 밖에 둔다. 경로·비밀번호는 사용자 홈의 ~/.gradle/gradle.properties에서 읽는다
    signingConfigs {
        create("release") {
            providers.gradleProperty("INTERACTIVE_STORE_FILE").orNull?.let { storeFile = file(it) }
            storePassword = providers.gradleProperty("INTERACTIVE_STORE_PASSWORD").orNull
            keyAlias = providers.gradleProperty("INTERACTIVE_KEY_ALIAS").orNull
            keyPassword = providers.gradleProperty("INTERACTIVE_KEY_PASSWORD").orNull
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            signingConfig = signingConfigs.getByName("release")
            // 코드 압축(R8)을 켜면 OkHttp가 시작하자마자 IllegalAccessError로 죽는다(2026-11-15 확인). 안전하게 끈다
            optimization {
                enable = false
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    useLibrary("android.car")
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.pleos.vehicle)
    implementation(libs.maplibre)
    implementation(libs.okhttp)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}