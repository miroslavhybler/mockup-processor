package mir.oslav.mockup.processor.generation

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import mir.oslav.mockup.processor.data.MockupObjectMember
import java.io.OutputStream


/**
 * Generates legacy `Mockup.providerName` extension accessors and their backing provider list.
 * @param outputStream Target KSP file stream.
 * @param targetPackageName Package where extension accessors should be generated.
 * @author Miroslav Hýbler <br>
 * created on 19.12.2025
 * @since 2.0.0
 */
class MockupObjectExtensionGenerator constructor(
    private val outputStream: OutputStream,
    private val targetPackageName: String,
) {

    /**
     * Generates extensions for getting data providers.
     * @param providers List of generated providers which are going to be accessible as public object's
     * properties.
     * @since 2.0.0
     */
    fun generate(
        providers: List<MockupObjectMember>,
    ) {
        val fileBuilder = FileSpec.builder(packageName = targetPackageName, fileName = "EXTENSIONS")

        providers.forEach { provider ->
            fileBuilder.addProperty(provider.createBackingProviderProperty())
        }

        fileBuilder.addProperty(createProvidersListProperty(providers = providers))

        providers.forEach { provider ->
            fileBuilder.addProperty(provider.createMockupExtensionProperty())
        }

        fileBuilder.build().writeGeneratedFileTo(outputStream, includeHeader = false)
    }

    /**
     * Creates a private singleton provider property used by generated extension getters.
     */
    private fun MockupObjectMember.createBackingProviderProperty(): PropertySpec {
        return PropertySpec.builder(backingPropertyName, toProviderClassName(), KModifier.PRIVATE)
            .initializer("%T()", toProviderClassName())
            .build()
    }

    /**
     * Creates the internal list consumed by generated data lookup helpers.
     */
    private fun createProvidersListProperty(
        providers: List<MockupObjectMember>,
    ): PropertySpec {
        val mockupDataProviderType = ClassName("com.mockup.core", "MockupDataProvider")
        val providersListType = ClassName("kotlin.collections", "List")
            .parameterizedBy(mockupDataProviderType.parameterizedBy(STAR))

        val providerReferences = providers.joinToString(separator = ", ") { provider ->
            provider.backingPropertyName
        }

        return PropertySpec.builder("providersList", providersListType, KModifier.INTERNAL)
            .initializer("listOf(%L)", providerReferences)
            .build()
    }

    /**
     * Creates a deprecated extension property on `Mockup` for this provider.
     */
    private fun MockupObjectMember.createMockupExtensionProperty(): PropertySpec {
        val providerType = toProviderClassName()
        val mockupType = ClassName("com.mockup.core", "Mockup")

        return PropertySpec.builder(providerClassName.decapitalized(), providerType, KModifier.PUBLIC)
            .receiver(mockupType)
            .addAnnotation(createDeprecationAnnotation())
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L", backingPropertyName)
                    .build()
            )
            .build()
    }

    /**
     * Creates the deprecation annotation used by generated extension properties.
     */
    private fun MockupObjectMember.createDeprecationAnnotation(): AnnotationSpec {
        return AnnotationSpec.builder(Deprecated::class)
            .addMember(
                "message = %S",
                "Generated extensions will be removed in v2.x.x, using Mockup.get() as replacement.",
            )
            .addMember(
                "replaceWith = %T(expression = %S)",
                ReplaceWith::class,
                "Mockup.get<${providerClassName}>()",
            )
            .build()
    }

    /**
     * Name of the generated private property that stores a provider instance.
     */
    private val MockupObjectMember.backingPropertyName: String
        get() = "m$providerClassName"

    /**
     * Converts this provider descriptor to its generated provider type.
     */
    private fun MockupObjectMember.toProviderClassName(): ClassName {
        return ClassName(packageName = providerClassPackage, providerClassName)
    }
}
