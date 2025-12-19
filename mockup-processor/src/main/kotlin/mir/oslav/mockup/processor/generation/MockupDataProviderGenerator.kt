package mir.oslav.mockup.processor.generation

import com.mockup.annotations.Mockup
import mir.oslav.mockup.processor.MockupConstants
import mir.oslav.mockup.processor.data.MockupType
import java.io.OutputStream


/**
 * Generator for concrete mockup data providers. All generated provider classes are extending abstract
 * generated MockupDataProvider class.
 * @see AbstractMockupDataProviderGenerator
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 16.09.2023
 */
class MockupDataProviderGenerator constructor(

) {

    /**
     * @param outputStream Output stream where generated code will be written
     * @param clazz [MockupType] representing class annotated with @[Mockup] annotation.
     * @param generatedValuesContent
     * @since 1.0.0
     * @return Class name of generated mockup data provider.
     */
    fun generateContent(
        outputStream: OutputStream,
        clazz: MockupType.MockUpped,
        generatedValuesContent: String
    ): String {
        val name = clazz.name
        val declaration = clazz.type.declaration
        val type = declaration.simpleName.getShortName()
        val providerClassName = "${name}MockupProvider"
        val writtenImports = ArrayList<String>()

        //Header, package name and import of base class
        outputStream += MockupConstants.GENERATED_FILE_HEADER
        outputStream += "\n\n"
        outputStream += "package com.mockup.providers"
        outputStream += "\n\n"
        outputStream += "import com.mockup.core.MockupDataProvider\n"

        //Used types imports
        clazz.imports.sortedDescending().forEach { qualifiedName ->
            if (!writtenImports.contains(element = qualifiedName)) {
                outputStream += "import $qualifiedName\n"
                writtenImports.add(qualifiedName)
            }
        }

        //Javadoc
        outputStream += "\n"
        outputStream += "/**\n"
        outputStream += " * Holds the generated mockup data for ${name} class.\n"
        outputStream += " * Single item can be accessed by [${providerClassName}.single] \n"
        outputStream += " * Multiple items with [${providerClassName}.list].\n"
        outputStream += " * @since 1.0.0\n"
        outputStream += " */\n"


        //Class definition
        outputStream += "public class ${providerClassName} internal constructor(): MockupDataProvider<${type}>(\n"
        outputStream += "\tclazz = ${type}::class,\n"
        outputStream += "\tvalues = $generatedValuesContent\n"
        outputStream += ") {\n"
        outputStream += "}"

        return providerClassName
    }

}