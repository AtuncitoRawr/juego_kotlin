plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")

}

android {
    namespace = "com.example.juego_movil"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.juego_movil"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }


    kotlinOptions {
        jvmTarget = "11"
    }


    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

}


dependencies {


    // Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // 1. Core de Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    // 2. Conversor de Gson
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // 3. Interceptor de OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // 4. Soporte para Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ROOM
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")


}
