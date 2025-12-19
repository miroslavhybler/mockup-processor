package mir.oslav.mockup.processor.generation

import mir.oslav.mockup.processor.MockupConstants
import mir.oslav.mockup.processor.data.MockupObjectMember
import java.io.OutputStream
import java.util.Locale


/**
 * Generator of Mockup.kt object which is only way for getting generated data via its properties
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 15.09.2023
 */
@Deprecated(
    message = "Will be removed in 2.0.0 and replaced by com.mockup.core.Mockup object which won't be generated.",
    replaceWith = ReplaceWith(
        expression = "Mockup",
        imports = ["com.mockup.core.Mockup"]
    )
)
open class MockupObjectGenerator constructor(
    protected val outputStream: OutputStream
) {


    /**
     * Generates Mockup.kt object code containing properties defined by [providers].
     * @param providers List of generated providers which are going to be accessible as public object's
     * properties.
     * @since 1.0.0
     */
    fun generateContent(providers: List<MockupObjectMember>) {

        //Header
        //TODO change package to com.mockup since 2.0.0
        outputStream += MockupConstants.GENERATED_FILE_HEADER
        outputStream += "\n\n"
        outputStream += "package com.mockup"
        outputStream += "\n\n"

        //Imports
        providers.forEach { provider ->
            val import = "import ${provider.providerClassPackage}.${provider.providerClassName}"
            outputStream += "$import\n"
        }

        outputStream += "\n"

        //Mockup object documentation
        outputStream += "/**\n"
        outputStream += " * All generated data are accessed via public properties on Mockup object.\n"
        outputStream += " * @since 1.0.0\n"
        outputStream += " */\n"

        outputStream += "public object Mockup {\n"

        //Mockup data providers as public values of object
        providers.forEach { provider ->
            outputStream += "\n"

            //Item documentation
            outputStream += "\t/**\n"
            outputStream += "\t * Provides generated mockup data for ${provider.propertyName}\n"
            outputStream += "\t */\n"
            val valName = provider.propertyName.replaceFirstChar { char ->
                char.lowercase(locale = Locale.ROOT)
            }
            val valDeclaration = "public val $valName"
            val valType = ": ${provider.providerClassName}"
            val constructorCall = "${provider.providerClassName}()"

            outputStream += "\t$valDeclaration$valType = $constructorCall\n"
            outputStream += "\n"
        }


        outputStream += "}"
    }
}