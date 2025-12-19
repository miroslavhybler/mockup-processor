plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "com.mockup.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mockup.example"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "1.2.2"

        setProperty("archivesBaseName", "KSPMockupExample-$versionName-build-$versionCode")
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
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"

        freeCompilerArgs += listOf(
            "-Xopt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-Xopt-in=androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi",
            "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xopt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi"
        )
    }
    kotlin {
        jvmToolchain(jdkVersion = 11)
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    ksp {
        //You can set custom dateTime format for the date generation
        //https://www.joda.org/joda-time/key_format.html
        arg(k = "mockup-date-format", v = "yyyy-MM-dd HH:mm:ss Z")
    }
}

dependencies {

    /** Mockup plugin */
    //Always keep same version for processor and annotations
    implementation(libs.mockup.annotations)
    implementation(project(":mockup-core"))

    //use kspDebug since mockup is meant to be only for compose preview in debug mode
    ksp(project(":mockup-processor"))

    /** Compose and material */
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)

    /** Coil library for images loading */
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
