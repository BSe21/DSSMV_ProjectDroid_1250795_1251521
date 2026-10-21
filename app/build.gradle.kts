plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bettergym"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
        buildFeatures {
            buildConfig = true
        }
    }

    defaultConfig {
        applicationId = "com.example.bettergym"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.12.0")


    implementation("com.google.android.gms:play-services-maps:20.1.0")

    // Para obter o GPS do utilizador
    implementation("com.google.android.gms:play-services-location:21.4.0")
    // 1. A BoM do Firebase (já tinhas)
    implementation(platform("com.google.firebase:firebase-bom:35.0.0"))

    // 2. ADICIONA ESTAS DUAS LINHAS QUE FALTAVAM:
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // O resto que já tinhas:
    implementation("androidx.activity:activity:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth:25.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
