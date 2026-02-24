@file:Suppress("UnstableApiUsage")
//include(":example-local")


pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        mavenLocal()
    }
}

rootProject.name = "ksp-mockup"
include(":mockup-processor")
if (System.getenv("JITPACK") != "true") {
    includeBuild("mockup-core")
    include(":example-app")
    include(":example-data")

}
