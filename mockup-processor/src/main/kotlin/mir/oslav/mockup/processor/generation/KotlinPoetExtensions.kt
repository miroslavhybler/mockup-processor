package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
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
