
import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

// 1. LER O FICHEIRO local.properties (Sintaxe Kotlin)
val properties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.bettergym"
    compileSdk = 35 // Simplifiquei para a versão padrão e estável

    defaultConfig {
        applicationId = "com.example.bettergym"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val googleApiUrl = properties.getProperty("GOOGLE_API_URL") ?: ""
        buildConfigField("String", "GOOGLE_API_URL", "\"$googleApiUrl\"")

        val baseUrl = properties.getProperty("BASE_URL") ?: ""
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        // ATENÇÃO: Se a tua chave se chamar GOOGLE_PLACES_API_KEY no local.properties
        val apiKey = properties.getProperty("GOOGLE_PLACES_API_KEY") ?: ""
        buildConfigField("String", "GOOGLE_PLACES_API_KEY", "\"$apiKey\"")
    }

    // 3. ATIVAR A CLASSE BuildConfig (Tem de ficar solto dentro do bloco 'android')
    buildFeatures {
        buildConfig = true
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

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:35.0.0"))

    // Auth e Firestore
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // O resto que já tinhas:
    implementation("androidx.activity:activity:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    implementation("com.google.firebase:firebase-analytics")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}