plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.interactive"
    compileSdk {
        version = release(37)
    }
    // AAOS 표준 차량 API(CarPropertyManager, CarUxRestrictionsManager)
    useLibrary("android.car")

    defaultConfig {
        applicationId = "com.example.interactive"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.app.automotive)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation("ai.pleos.playground:Vehicle:2.3.2")
    implementation("org.maplibre.gl:android-sdk:13.7.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}