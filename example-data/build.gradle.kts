plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization.json)
}

android {
    namespace = "com.mockup.exampledata"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    ksp {
        //You can set custom dateTime format for the date generation
        //https://www.joda.org/joda-time/key_format.html
        arg(k = "mockup-date-format", v = "yyyy-MM-dd HH:mm:ss Z")
    }
}

kotlin {
    jvmToolchain(jdkVersion = 11)
}

dependencies {

    /** Mockup */
    //Always keep same version for mockup dependencies
    implementation("com.github.miroslavhybler:mockup-annotations:2.0.0-alpha02")
    implementation("com.github.miroslavhybler:mockup-core:2.0.0-alpha02")
    //use kspDebug since mockup is meant to be only for compose preview in debug mode
    ksp(project(":mockup-processor"))
    //tooling preview required as providers implements PreviewParameterProvider
    implementation(libs.compose.ui.tooling.preview)


    implementation(libs.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}