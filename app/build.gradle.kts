import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.service)
}

android {
    namespace = "ir.alishojaee.mathforest"
    compileSdk = 36

    defaultConfig {
        applicationId = "ir.alishojaee.mathforest"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        buildConfigField(
            "String",
            "TAPSELL_KEY",
            gradleLocalProperties(rootDir, providers).getProperty("TAPSELL_KEY")
        )
        buildConfigField(
            "String",
            "TAPSELL_STANDARD_ZONE_ID",
            gradleLocalProperties(rootDir, providers).getProperty("TAPSELL_STANDARD_ZONE_ID")
        )
        buildConfigField(
            "String",
            "TAPSELL_INSTERTITIAL_ZONE_ID",
            gradleLocalProperties(rootDir, providers).getProperty("TAPSELL_INSTERTITIAL_ZONE_ID")
        )
        buildConfigField(
            "String",
            "TAPSELL_REWARDED_VIDEO_ZONE_ID",
            gradleLocalProperties(rootDir, providers).getProperty("TAPSELL_REWARDED_VIDEO_ZONE_ID")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.tapsell.plus.sdk.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics)
}