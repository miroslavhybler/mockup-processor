package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import mir.oslav.mockup.processor.data.MockupProviderHintData
import java.io.OutputStream

/**
 * Generates the `com.mockup.GeneratedMockupRegistry` file used to register custom mockup providers
 * and expose runtime diagnostics for erased generic/typealias providers.
 * @param outputStream Target KSP file stream.
 * @since 2.0.0
 */
class MockupRegistryGenerator(
    private val outputStream: OutputStream,
) {

    /**
     * Writes the generated registry file.
     * @param providers Custom provider declarations discovered in the processed module.
     * @param providerHints Generated provider hints for concrete generic/typealias providers.
     * @since 2.0.0
     */
    fun generate(
        providers: List<KSClassDeclaration>,
        providerHints: List<MockupProviderHintData>,
    ) {
        FileSpec.builder(packageName = "com.mockup", fileName = "GeneratedMockupRegistry")
            .addType(
                TypeSpec.objectBuilder("GeneratedMockupRegistry")
                    .addProperty(
                        PropertySpec.builder("isRegistered", Boolean::class, KModifier.PRIVATE)
                            .mutable()
                            .initializer("false")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("register")
                            .addCode(createRegisterCode(providers = providers))
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("providerHints")
                            .addAnnotation(JvmStatic::class)
                            .returns(
                                ClassName("kotlin.collections", "List")
                                    .parameterizedBy(
                                        ClassName("com.mockup", "GeneratedMockupRegistry", "ProviderHint")
                                    )
                            )
                            .addCode(createProviderHintsCode(providerHints = providerHints))
                            .build()
                    )
                    .addType(createProviderHintType())
                    .build()
            )
            .build()
            .writeGeneratedFileTo(outputStream)
    }

    /**
     * Builds the body of `GeneratedMockupRegistry.register()`.
     * @param providers Custom providers to register.
     * @return Code block that guards repeated registration and forwards providers to `Mockup`.
     */
    private fun createRegisterCode(
        providers: List<KSClassDeclaration>,
    ): CodeBlock {
        val customProviderType = ClassName("com.mockup.core", "CustomMockupProvider")
        val mockupType = ClassName("com.mockup.core", "Mockup")
        val providersListType = ClassName("kotlin.collections", "List")
            .parameterizedBy(customProviderType.parameterizedBy(STAR))

        return CodeBlock.builder()
            .addStatement("if (isRegistered) return")
            .addStatement("isRegistered = true")
            .add("val providers: %T = listOf(\n", providersListType)
            .indent()
            .apply {
                providers.forEach { provider ->
                    add("%L,\n", provider.providerInstanceCode())
                }
            }
            .unindent()
            .add(")\n")
            .add("providers.forEach { provider ->\n")
            .indent()
            .addStatement("%T.register(provider)", mockupType)
            .unindent()
            .add("}\n")
            .build()
    }

    /**
     * Builds `GeneratedMockupRegistry.providerHints()` for runtime error messages.
     * @param providerHints Generated concrete generic/typealias provider descriptions.
     * @return Code block returning a list of core diagnostic hint objects.
     */
    private fun createProviderHintsCode(
        providerHints: List<MockupProviderHintData>,
    ): CodeBlock {
        if (providerHints.isEmpty()) {
            return CodeBlock.of("return emptyList()\n")
        }

        val hintType = ClassName("com.mockup", "GeneratedMockupRegistry", "ProviderHint")
        return CodeBlock.builder()
            .add("return listOf(\n")
            .indent()
            .apply {
                providerHints.forEach { hint ->
                    add(
                        "%T(\n" +
                                "rawClassName = %S,\n" +
                                "targetTypeName = %S,\n" +
                                "providerClassName = %S,\n" +
                                "accessorName = %S,\n" +
                                "accessorImport = %S,\n" +
                                "),\n",
                        hintType,
                        hint.rawClassName,
                        hint.targetTypeName,
                        hint.qualifiedProviderClassName,
                        hint.accessorName,
                        hint.accessorImport,
                    )
                }
            }
            .unindent()
            .add(")\n")
            .build()
    }

    /**
     * Creates the generated hint value class. It intentionally lives in generated code so older
     * published `mockup-core` artifacts can still compile projects generated by this processor.
     */
    private fun createProviderHintType(): TypeSpec {
        val stringType = String::class
        val properties = listOf(
            "rawClassName",
            "targetTypeName",
            "providerClassName",
            "accessorName",
            "accessorImport",
        )
        return TypeSpec.classBuilder("ProviderHint")
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .apply {
                        properties.forEach { propertyName ->
                            addParameter(ParameterSpec.builder(propertyName, stringType).build())
                        }
                    }
                    .build()
            )
            .apply {
                properties.forEach { propertyName ->
                    addProperty(
                        PropertySpec.builder(propertyName, stringType)
                            .initializer(propertyName)
                            .build()
                    )
                }
            }
            .build()
    }

    /**
     * Builds the expression used inside the registry provider list.
     * Objects are referenced directly; classes are instantiated with a no-argument constructor.
     */
    private fun KSClassDeclaration.providerInstanceCode(): CodeBlock {
        val providerType = toClassName()
        return if (classKind == ClassKind.OBJECT) {
            CodeBlock.of("%T", providerType)
        } else {
            CodeBlock.of("%T()", providerType)
        }
    }
}
