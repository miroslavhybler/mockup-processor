package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.Variance
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import mir.oslav.mockup.processor.MockupConstants
import mir.oslav.mockup.processor.data.MockupType
import java.io.OutputStream
import java.io.OutputStreamWriter

/**
 * Writes this [FileSpec] into a KSP-created [outputStream].
 * @param outputStream Target file stream from KSP's code generator.
 * @param includeHeader Whether to prepend the Mockup generated-file header.
 * @since 2.0.0
 */
internal fun FileSpec.writeGeneratedFileTo(
    outputStream: OutputStream,
    includeHeader: Boolean = true,
) {
    OutputStreamWriter(outputStream).also { writer ->
        if (includeHeader) {
            writer.write(MockupConstants.GENERATED_FILE_HEADER)
            writer.write("\n\n")
        }
        writeTo(writer)
        writer.flush()
    }
}

/**
 * Converts a mocked class descriptor to the KotlinPoet type used in generated provider code.
 * @return KotlinPoet class name including nested parent class names.
 * @since 2.0.0
 */
internal fun MockupType.MockUpped.toClassName(): ClassName {
    return declaration.toClassName()
}

/**
 * Converts a mocked class descriptor to the type exposed by the generated provider.
 *
 * Type aliases are preserved here so `MockupDataProvider<ListOfUsersResponse>` is generated for
 * alias providers, while regular generic usages are rendered as parameterized class names.
 * @since 2.0.0
 */
internal fun MockupType.MockUpped.toProviderTargetTypeName(): TypeName {
    return typeAlias?.toClassName() ?: toConstructorTypeName()
}

/**
 * Converts a mocked class descriptor to the concrete type used for constructor calls.
 * @since 2.0.0
 */
internal fun MockupType.MockUpped.toConstructorTypeName(): TypeName {
    return type.toTypeName()
}

/**
 * Converts an enum descriptor to the KotlinPoet type used for enum-entry references.
 * @return KotlinPoet class name including nested parent class names.
 * @since 2.0.0
 */
internal fun MockupType.Enum.toClassName(): ClassName {
    return (declaration as KSClassDeclaration).toClassName()
}

/**
 * Converts a KSP class declaration into a KotlinPoet [ClassName].
 * @return Class name preserving package and nested class hierarchy.
 * @since 2.0.0
 */
internal fun KSClassDeclaration.toClassName(): ClassName {
    return ClassName(
        packageName = packageName.asString(),
        simpleNames = parentClassNames() + simpleName.getShortName(),
    )
}

/**
 * Converts a KSP type into a KotlinPoet [TypeName], preserving concrete type arguments.
 * @since 2.0.0
 */
internal fun KSType.toTypeName(): TypeName {
    val classDeclaration = declaration as? KSClassDeclaration
        ?: error("Unable to generate KotlinPoet TypeName for ${declaration.simpleName.asString()}.")
    val className = classDeclaration.toClassName()
    val typeName = if (arguments.isEmpty()) {
        className
    } else {
        className.parameterizedBy(
            arguments.map { argument ->
                if (argument.variance == Variance.STAR || argument.type == null) {
                    STAR
                } else {
                    val argumentTypeName = argument.type!!.resolve().toTypeName()
                    when (argument.variance) {
                        Variance.COVARIANT -> WildcardTypeName.producerOf(argumentTypeName)
                        Variance.CONTRAVARIANT -> WildcardTypeName.consumerOf(argumentTypeName)
                        Variance.INVARIANT -> argumentTypeName
                        Variance.STAR -> STAR
                    }
                }
            }
        )
    }

    return typeName.copy(nullable = isMarkedNullable)
}

/**
 * Converts a KSP typealias declaration into a KotlinPoet [ClassName].
 * @since 2.0.0
 */
private fun KSTypeAlias.toClassName(): ClassName {
    return ClassName(packageName = packageName.asString(), simpleName.getShortName())
}

/**
 * Returns parent class names from outermost to innermost for nested declarations.
 * @since 2.0.0
 */
private fun KSDeclaration.parentClassNames(): List<String> {
    val parents = mutableListOf<String>()
    var parent = parentDeclaration as? KSClassDeclaration
    while (parent != null) {
        parents.add(parent.simpleName.getShortName())
        parent = parent.parentDeclaration as? KSClassDeclaration
    }
    return parents.asReversed()
}
