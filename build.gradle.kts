plugins {
    `kotlin-dsl`
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.test) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization.json) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dokka) apply false
}

val verifyMockupVariantIsolation by tasks.registering {
    group = "verification"
    description = "Checks that Mockup runtime and KSP dependencies are scoped to debug examples."

    doLast {
        val mockupArtifacts = listOf(
            "mockup-annotations",
            "mockup-core",
            "mockup-processor",
        )

        listOfNotNull(
            project.findProject(":example-app"),
            project.findProject(":example-data"),
        ).forEach { targetProject ->
            fun runtimeComponents(configurationName: String): Set<String> =
                targetProject.configurations
                    .getByName(configurationName)
                    .incoming
                    .resolutionResult
                    .allComponents
                    .map { component -> component.id.displayName }
                    .toSet()

            listOf("release", "benchmark").forEach variant@{ variant ->
                val runtimeConfiguration = targetProject.configurations
                    .findByName("${variant}RuntimeClasspath")
                    ?: return@variant
                val runtimeLeaks = runtimeComponents(runtimeConfiguration.name)
                    .filter { component ->
                        mockupArtifacts.any { artifact -> component.contains(artifact) }
                    }
                check(runtimeLeaks.isEmpty()) {
                    "${targetProject.path} $variant runtime contains Mockup artifacts: $runtimeLeaks"
                }

                val kspConfigurationName = "ksp" + variant.replaceFirstChar(Char::uppercaseChar)
                val processors = targetProject.configurations
                    .findByName(kspConfigurationName)
                    ?.allDependencies
                    .orEmpty()
                    .map { dependency -> dependency.name }
                    .filter { dependency -> dependency.contains("mockup-processor") }
                check(processors.isEmpty()) {
                    "${targetProject.path} $variant KSP contains Mockup processors: $processors"
                }
            }

            val debugRuntime = runtimeComponents("debugRuntimeClasspath")
            check(debugRuntime.any { component -> component.contains("mockup-core") }) {
                "${targetProject.path} debug runtime does not contain mockup-core"
            }

            val debugProcessors = targetProject.configurations
                .getByName("kspDebug")
                .allDependencies
                .map { dependency -> dependency.name }
            check(debugProcessors.any { dependency -> dependency.contains("mockup-processor") }) {
                "${targetProject.path} debug KSP does not contain mockup-processor"
            }
        }
    }
}

tasks.named("check").configure {
    dependsOn(verifyMockupVariantIsolation)
}
