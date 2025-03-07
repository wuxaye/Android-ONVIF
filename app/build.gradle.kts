import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.xaye.onvifexplorer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xaye.onvifexplorer"
        minSdk = 24
        targetSdk = 34
        versionCode = 11
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../platform.keystore")
            storePassword = "wif210301"
            keyAlias = "wif_autograph"
            keyPassword = "wif210301"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
        viewBinding = true
    }

    // 输出类型
    android.applicationVariants.all {
        // 编译类型
        val buildType = this.buildType.name
        val date = SimpleDateFormat("MMdd").format(Date())
        outputs.all {
            // 判断是否是输出 apk 类型
            if (this is com.android.build.gradle
                .internal.api.ApkVariantOutputImpl
            ) {
                this.outputFileName = "onvifexplorer" +
                        "_v${android.defaultConfig.versionName}_${date}_${buildType}.apk"
            }
        }
    }


}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(project(":OnvifDiscoveryKit"))//onvif
    implementation(project(":helper"))//快速开发库
    implementation("com.github.alexeyvasilyev:rtsp-client-android:5.3.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}