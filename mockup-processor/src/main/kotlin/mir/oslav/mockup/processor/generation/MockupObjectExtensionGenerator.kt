package mir.oslav.mockup.processor.generation

import mir.oslav.mockup.processor.data.MockupObjectMember
import java.io.OutputStream


/**
 * @author Miroslav Hýbler <br>
 * created on 19.12.2025
 * @since 2.0.0
 */
class MockupObjectExtensionGenerator constructor(
    private val outputStream: OutputStream
) {

    /**
     * Generates extensions for getting data providers.
     * @param providers List of generated providers which are going to be accessible as public object's
     * properties.
     * @since 2.0.0
     */
    fun generate(
        providers: List<MockupObjectMember>
    ) {
        outputStream += "package com.mockup"
        outputStream += "\n\n\n"

        outputStream += "import com.mockup.core.Mockup\n"
        outputStream += "import com.mockup.core.MockupDataProvider\n"

        providers.forEach { providers ->
            outputStream += "import ${providers.qualifiedName}\n"
        }

        outputStream += "\n\n"

        providers.forEach { provider ->
            outputStream += "private val ${provider.providerClassName.decapitalized()}: ${provider.providerClassName} = ${provider.providerClassName}()\n"
        }

        outputStream += "\n\n"


        outputStream += "internal val providersList: List<MockupDataProvider<*>> = listOf("
        providers.forEach { provider ->
            outputStream += "\t${provider.providerClassName.decapitalized()},\n"
        }
        outputStream += ")"

        outputStream += "\n\n"

        providers.forEach { provider ->
            outputStream += "public val Mockup.${provider.propertyName.decapitalized()}: ${provider.providerClassName}\n"
            outputStream += "\tget() = ${provider.providerClassName.decapitalized()}\n"
            outputStream += "\n\n"
        }

    }
}