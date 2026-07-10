package mir.oslav.mockup.processor.generation

import com.mockup.annotations.Mockup
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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
        generatedValuesContent: CodeBlock,
        packageName: String,
        usePreviewParameterProviders: Boolean,
    ): String {
        val name = clazz.providerName
        val providerClassName = "${name}MockupProvider"
        val providerType = ClassName(packageName, providerClassName)
        val targetType = clazz.toProviderTargetTypeName()
        val rawTargetType = clazz.toClassName()
        val mockupDataProviderType = ClassName("com.mockup.core", "MockupDataProvider")
        val generatedRegistryType = ClassName("com.mockup", "GeneratedMockupRegistry")
        val kClassType = ClassName("kotlin.reflect", "KClass").parameterizedBy(targetType)
        val requiresClazzCast = clazz.requiresGeneratedAccessor

        val mockupDataProviderOfTarget = mockupDataProviderType.parameterizedBy(targetType)
        val providerBuilder = TypeSpec.classBuilder(providerType)
            .addKdoc(
                "Holds the generated mockup data for %L class.\n" +
                        "Single item can be accessed by [%L.single]\n" +
                        "Multiple items with [%L.list].\n" +
                        "@since 1.0.0\n",
                name,
                providerClassName,
                providerClassName,
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addModifiers(KModifier.INTERNAL)
                    .build()
            )
            .superclass(mockupDataProviderOfTarget)
            .apply {
                if (requiresClazzCast) {
                    addAnnotation(
                        AnnotationSpec.builder(Suppress::class)
                            .addMember("%S", "UNCHECKED_CAST")
                            .build()
                    )
                    addSuperclassConstructorParameter(
                        "clazz = %T::class as %T",
                        rawTargetType,
                        kClassType,
                    )
                } else {
                    addSuperclassConstructorParameter("clazz = %T::class", targetType)
                }
            }
            .addSuperclassConstructorParameter(
                "values = %L",
                createValuesCodeBlock(
                    generatedRegistryType = generatedRegistryType,
                    generatedValuesContent = generatedValuesContent,
                )
            )

        if (usePreviewParameterProviders) {
            val previewParameterProviderType = ClassName(
                packageName = "androidx.compose.ui.tooling.preview",
                simpleNames = listOf("PreviewParameterProvider"),
            )
            providerBuilder.addSuperinterface(
                superinterface = previewParameterProviderType.parameterizedBy(
                    targetType
                )
            )
            providerBuilder.addProperty(
                PropertySpec.builder(name = "count", type = Int::class, KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addStatement("return super<%T>.count", mockupDataProviderType)
                            .build()
                    )
                    .build()
            )
        }

        FileSpec.builder(packageName, fileName = providerClassName)
            .addType(typeSpec = providerBuilder.build())
            .build()
            .writeGeneratedFileTo(outputStream)

        return providerClassName
    }

    private fun createValuesCodeBlock(
        generatedRegistryType: ClassName,
        generatedValuesContent: CodeBlock,
    ): CodeBlock {
        return CodeBlock.builder()
            .beginControlFlow("run")
            .addStatement("%T.register()", generatedRegistryType)
            .add("%L\n", generatedValuesContent)
            .endControlFlow()
            .build()
    }

}
