plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")

    kotlin("kapt") // Plugin para Kotlin Annotation Processing Tool (KAPT)

}

android {
    namespace = "br.edu.ifsp.dmo.whatsapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.edu.ifsp.dmo.whatsapp"
        minSdk = 32
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

    viewBinding{
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    //dependencias firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.analytics)
    // Dependência para a biblioteca do Realtime Database
    implementation(libs.firebase.database)
    // Dependência para a biblioteca do Firebase Auth
    implementation(libs.firebase.auth)
    // Dependência para a biblioteca do Firebase Storage
    implementation(libs.firebase.storage)

    implementation(libs.glide) // Glide para carregamento de imagens
    kapt(libs.compiler) // KAPT para processar as anotações do Glide


}