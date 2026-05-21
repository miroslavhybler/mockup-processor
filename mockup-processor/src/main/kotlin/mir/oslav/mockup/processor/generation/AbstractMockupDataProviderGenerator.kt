package mir.oslav.mockup.processor.generation

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
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
)
class AbstractMockupDataProviderGenerator constructor(
    private val outputStream: OutputStream,
) {

    /**
     * Writes code into [outputStream]
     * @since 1.0.0
     */
    fun generateContent() {
        val typeVariable = TypeVariableName(
            name = "T",
            bounds = listOf(ClassName("kotlin", "Any")),
        )
        val sequenceType = ClassName("kotlin.sequences", "Sequence").parameterizedBy(typeVariable)
        val listType = ClassName("kotlin.collections", "List").parameterizedBy(typeVariable)
        val previewParameterProviderType =
            ClassName("androidx.compose.ui.tooling.preview", "PreviewParameterProvider")

        FileSpec.builder(packageName = "com.mockup", fileName = "MockupDataProvider")
            .addType(
                TypeSpec.classBuilder("MockupDataProvider")
                    .addModifiers(KModifier.PUBLIC, KModifier.ABSTRACT)
                    .addTypeVariable(typeVariable)
                    .addKdoc(
                        "Defines the mockup data provider class\n" +
                                "@param values Generated mockup data, must be not empty\n" +
                                "@since 1.0.O\n"
                    )
                    .primaryConstructor(
                        FunSpec.constructorBuilder()
                            .addParameter(
                                ParameterSpec.builder("values", sequenceType)
                                    .defaultValue("%M()", MemberName("kotlin.sequences", "emptySequence"))
                                    .build()
                            )
                            .build()
                    )
                    .addSuperinterface(previewParameterProviderType.parameterizedBy(typeVariable))
                    .addProperty(
                        PropertySpec.builder("values", sequenceType, KModifier.OVERRIDE)
                            .initializer("values")
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("single", typeVariable)
                            .getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return values.first()")
                                    .build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("list", listType)
                            .getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return values.toList()")
                                    .build()
                            )
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("random", typeVariable)
                            .getter(
                                FunSpec.getterBuilder()
                                    .addStatement("return list.random()")
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
            .writeGeneratedFileTo(outputStream)
    }
}
