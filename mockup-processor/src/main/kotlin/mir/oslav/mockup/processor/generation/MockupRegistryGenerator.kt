package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import java.io.OutputStream

class MockupRegistryGenerator(
    private val outputStream: OutputStream,
) {

    fun generate(providers: List<KSClassDeclaration>) {
        FileSpec.builder(packageName = "com.mockup", fileName = "GeneratedMockupRegistry")
            .addType(
                TypeSpec.objectBuilder("GeneratedMockupRegistry")
                    .addModifiers(KModifier.INTERNAL)
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
                    .build()
            )
            .build()
            .writeGeneratedFileTo(outputStream)
    }

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

    private fun KSClassDeclaration.providerInstanceCode(): CodeBlock {
        val providerType = toClassName()
        return if (classKind == ClassKind.OBJECT) {
            CodeBlock.of("%T", providerType)
        } else {
            CodeBlock.of("%T()", providerType)
        }
    }
}
