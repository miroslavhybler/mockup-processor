package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import mir.oslav.mockup.processor.MockupConstants
import mir.oslav.mockup.processor.data.MockupType
import java.io.OutputStream
import java.io.OutputStreamWriter

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

internal fun MockupType.MockUpped.toClassName(): ClassName {
    return declaration.toClassName()
}

internal fun MockupType.Enum.toClassName(): ClassName {
    return (declaration as KSClassDeclaration).toClassName()
}

internal fun KSClassDeclaration.toClassName(): ClassName {
    return ClassName(
        packageName = packageName.asString(),
        simpleNames = parentClassNames() + simpleName.getShortName(),
    )
}

private fun KSDeclaration.parentClassNames(): List<String> {
    val parents = mutableListOf<String>()
    var parent = parentDeclaration as? KSClassDeclaration
    while (parent != null) {
        parents.add(parent.simpleName.getShortName())
        parent = parent.parentDeclaration as? KSClassDeclaration
    }
    return parents.asReversed()
}
