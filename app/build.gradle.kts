import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id ("com.google.devtools.ksp")
//    id("androidx.navigation.safeargs.kotlin") version "2.8.4"
}

android {
    namespace = "com.example.skincure"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.skincure"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "BASE_URL", "\"https://skincure-backend-v2-1002330865172.asia-southeast1.run.app\"")


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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation(libs.logging.interceptor)

    //coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //viewmodel
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)

    //picasso
    implementation (libs.picasso)
    implementation (libs.picasso.transformations)

    //navigation
    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    //circleimageview
    implementation (libs.circleimageview)

    //firebase authentication
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation (libs.firebase.appcheck.playintegrity)
    implementation (libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.gms.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)
    implementation (libs.firebase.storage)

    //animation lottie
    implementation (libs.lottie)

    //cameraX
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation (libs.guava)

    //room
    implementation (libs.androidx.room.runtime)
    ksp (libs.androidx.room.compiler)
    implementation (libs.room.ktx)
    implementation("androidx.paging:paging-common-ktx:3.3.5")
    implementation(libs.androidx.room.paging)
    implementation("androidx.paging:paging-runtime-ktx:3.3.5")

    //shimmer
    implementation (libs.shimmer)
}