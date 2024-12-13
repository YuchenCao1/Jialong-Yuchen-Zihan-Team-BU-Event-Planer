plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.bueventplaner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bueventplaner"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.ui.test.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(platform("com.google.firebase:firebase-bom:32.2.0"))
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    // Jetpack Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.3")

    implementation ("com.google.accompanist:accompanist-pager:0.30.1")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.30.1")


    implementation ("androidx.compose.foundation:foundation:1.5.0")
    // Optional: For animations
    implementation ("androidx.compose.animation:animation:1.5.0")

    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.compose.material3:material3-window-size-class:1.2.0")

    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation ("com.google.accompanist:accompanist-pager:0.30.1")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.30.1")
    implementation ("com.google.firebase:firebase-storage-ktx:20.2.0")

    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.android.gms:play-services-auth")

    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.firebase:firebase-auth-ktx:22.1.1")


    implementation ("com.google.maps.android:maps-compose:2.11.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    kapt("androidx.room:room-compiler:2.5.2")

    implementation ("com.google.code.gson:gson:2.9.0")
    implementation ("io.github.boguszpawlowski.composecalendar:composecalendar:1.3.0")
    implementation ("io.github.boguszpawlowski.composecalendar:kotlinx-datetime:1.3.0")

}