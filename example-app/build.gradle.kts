plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.mockup.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mockup.example"
        minSdk = 21
        targetSdk = 35
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
    implementation(dependencyNotation =  "com.github.miroslavhybler:mockup-annotations:1.2.4")

    //use kspDebug since mockup is meant to be only for compose preview in debug mode
    ksp(dependencyNotation = project(":mockup-processor"))

    /** Compose and material */
    implementation(dependencyNotation = "androidx.core:core-ktx:1.15.0")
    implementation(dependencyNotation = "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation(dependencyNotation = "androidx.activity:activity-compose:1.10.1")
    implementation(dependencyNotation = platform("androidx.compose:compose-bom:2025.03.00"))
    implementation(dependencyNotation = "androidx.compose.ui:ui")
    implementation(dependencyNotation = "androidx.compose.ui:ui-graphics")
    implementation(dependencyNotation = "androidx.compose.ui:ui-tooling-preview")
    implementation(dependencyNotation = "androidx.compose.material3:material3")
    implementation(dependencyNotation = "androidx.navigation:navigation-compose:2.8.9")

    /** Coil library for images loading */
    implementation(dependencyNotation = "io.coil-kt:coil-compose:2.7.0")

    testImplementation(dependencyNotation = "junit:junit:4.13.2")
    androidTestImplementation(dependencyNotation = "androidx.test.ext:junit:1.2.1")
    androidTestImplementation(dependencyNotation = "androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(dependencyNotation = platform("androidx.compose:compose-bom:2025.03.00"))
    androidTestImplementation(dependencyNotation = "androidx.compose.ui:ui-test-junit4")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-tooling")
    debugImplementation(dependencyNotation = "androidx.compose.ui:ui-test-manifest")
}