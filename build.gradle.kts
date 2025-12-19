plugins {
    `kotlin-dsl`
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
}
