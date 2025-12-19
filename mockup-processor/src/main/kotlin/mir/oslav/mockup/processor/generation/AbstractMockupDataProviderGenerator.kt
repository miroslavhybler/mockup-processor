package mir.oslav.mockup.processor.generation

import mir.oslav.mockup.processor.MockupConstants
import java.io.OutputStream


/**
 * Generates MockupDataProvider abstract class which defines the mockup providers. Generated class has: <br>
 * <ul>
 *     <li>One generic null safe parameter T</li>
 *     <li>One primary constructor with values member which is List of T</li>
 *     <li>Property single - getter which takes random item from values</li>
 *     <li>Property list - getter which returns values property</li>
 * </ul>
 * @param outputStream Target output stream. The file of this stream should be named MockupDataProvider.kt
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 16.09.2023
 */
@Deprecated(
    message = "Will be removed in 2.0.0 and replaced by com.mockup.core.AbstractMockupDataProviderGenerator which won't be generated.",
    replaceWith = ReplaceWith(
        expression = "Mockup",
        imports = ["com.mockup.core.AbstractMockupDataProviderGenerator"]
    )
)class AbstractMockupDataProviderGenerator constructor(
    private val outputStream: OutputStream,
) {


    /**
     * Writes code into [outputStream]
     * @since 1.0.0
     */
    fun generateContent() {

        //File header
        outputStream += MockupConstants.GENERATED_FILE_HEADER
        outputStream += "\n\n\n"
        outputStream += "package com.mockup"
        outputStream += "\n\n\n"


        outputStream += "import androidx.compose.ui.tooling.preview.PreviewParameterProvider"
        outputStream += "\n"

        //Javadoc
        outputStream += "/**\n"
        outputStream += " * Defines the mockup data provider class\n"
        outputStream += " * @param values Generated mockup data, must be not empty\n"
        outputStream += " * @since 1.0.O\n"
        outputStream += " */\n"

        //Code
        outputStream += "public abstract class MockupDataProvider<T : Any> constructor(\n"
        outputStream += "\t override val values: Sequence<T> = emptySequence()\n"
        outputStream += "): PreviewParameterProvider<T> {\n"
        outputStream += "\n"
        outputStream += "\tval single: T get() = values.first()\n"
        outputStream += "\n"
        outputStream += "\tval list: List<T> get() = values.toList()\n"
        outputStream += "\n"
        outputStream += "\tval random: T get() = list.random()\n"
        outputStream += "}"
    }

}