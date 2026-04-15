package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import mir.oslav.mockup.processor.MockupConstants
import java.io.OutputStream

class MockupRegistryGenerator(
    private val outputStream: OutputStream,
) {

    fun generate(providers: List<KSClassDeclaration>) {
        outputStream += MockupConstants.GENERATED_FILE_HEADER
        outputStream += "\n\n"
        outputStream += "package com.mockup\n"
        outputStream += "\n"
        outputStream += "import com.mockup.core.CustomMockupProvider\n"
        outputStream += "import com.mockup.core.Mockup\n"
        outputStream += "\n"
        outputStream += "internal object GeneratedMockupRegistry {\n"
        outputStream += "\tprivate var isRegistered: Boolean = false\n"
        outputStream += "\tfun register() {\n"
        outputStream += "\t\tif (isRegistered) return\n"
        outputStream += "\t\tisRegistered = true\n"
        outputStream += "\t\tval providers: List<CustomMockupProvider<*>> = listOf(\n"

        providers.forEachIndexed { index, provider ->
            val qualifiedName = provider.qualifiedName?.asString() ?: return@forEachIndexed
            val providerInstance = if (provider.classKind == ClassKind.OBJECT) {
                qualifiedName
            } else {
                "$qualifiedName()"
            }
            outputStream += "\t\t\t$providerInstance"
            if (index != providers.lastIndex) {
                outputStream += ","
            }
            outputStream += "\n"
        }

        outputStream += "\t\t)\n"
        outputStream += "\t\tproviders.forEach { provider ->\n"
        outputStream += "\t\t\tMockup.register(provider)\n"
        outputStream += "\t\t}\n"
        outputStream += "\t}\n"
        outputStream += "}\n"
    }
}
