import org.jetbrains.dokka.DokkaConfiguration.Visibility

plugins {
    kotlin("jvm")
    id("kotlin-kapt")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("com.google.devtools.ksp")
}

group = "com.github.miroslavhybler.mockup-processor"
version = "1.2.4"

kotlin {
    jvmToolchain(jdkVersion = 11)
}

ksp {
    arg("ksp.debug", "true")
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation("com.github.miroslavhybler:mockup-annotations:1.2.4")
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.10-1.0.29")
    implementation("androidx.annotation:annotation:1.9.1")
    val autoServiceVersion = "1.1.1"
    kapt("com.google.auto.service:auto-service:$autoServiceVersion")
    compileOnly("com.google.auto.service:auto-service-annotations:$autoServiceVersion")
}


tasks.register("runMockupProcessor", JavaExec::class) {
    group = "Mockup"
    description = "Runs the KSP processor"

    classpath = sourceSets.getByName("main").runtimeClasspath

    this.mainClass.set("com.google.devtools.ksp.processing.Main")
    args("--symbol", "mir.oslav.mockup.processor.MockupProcessor")
}


tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("dokkaHtml"))

    dokkaSourceSets {
        configureEach {
            pluginsMapConfiguration.set(
                mutableMapOf(
                    "org.jetbrains.dokka.base.DokkaBase" to """{ "separateInheritedMembers": true}"""
                )
            )
            documentedVisibilities.set(
                mutableListOf(
                    Visibility.PUBLIC,
                    Visibility.PRIVATE,
                    Visibility.PROTECTED,
                    Visibility.INTERNAL,
                    Visibility.PACKAGE
                )
            )

            skipEmptyPackages.set(true)
            includeNonPublic.set(true)
            skipDeprecated.set(false)
            reportUndocumented.set(true)
            includes.from("${projectDir}/packages.md")
            description = ""
        }
    }
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("kotlin"))
                groupId = "com.github.miroslavhybler"
                artifactId = "mockup-processor"
                version = "1.2.4"
                pom {
                    description.set("Jitpack.io deploy")
                }
            }

        }
        repositories {
            mavenLocal()
        }
    }
}