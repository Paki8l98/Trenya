plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "space.khay.trenya"
    compileSdk = 34

    defaultConfig {
        applicationId = "space.khay.trenya"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {

    implementation ("com.google.android.gms:play-services-auth:19.2.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("androidx.viewpager:viewpager:1.0.0")
    implementation ("com.google.android.material:material:1.4.0")
    implementation ("androidx.recyclerview:recyclerview:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.22")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("androidx.room:room-common:2.6.1")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.compose.foundation:foundation-android:1.6.7")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}