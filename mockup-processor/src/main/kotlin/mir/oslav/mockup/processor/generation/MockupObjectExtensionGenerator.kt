package mir.oslav.mockup.processor.generation

import mir.oslav.mockup.processor.data.MockupObjectMember
import java.io.OutputStream


/**
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
        val writtenImports = ArrayList<String>()

        outputStream += "package $targetPackageName"
        outputStream += "\n\n\n"

        outputStream += "import com.mockup.core.Mockup\n"
        outputStream += "import com.mockup.core.MockupDataProvider\n"

        val imports = buildList {
            providers.forEach { provider ->
                provider.parentQualifiedName?.let(block = ::add)
                add(element = provider.qualifiedName)
            }
        }

        imports.distinct().forEach { import ->
            outputStream += "import $import\n"
        }

        outputStream += "\n\n"

        providers.forEach { provider ->
            outputStream += "private val m${provider.providerClassName}: ${provider.providerClassName} = ${provider.providerClassName}()\n"
        }

        outputStream += "\n\n"


        outputStream += "internal val providersList: List<MockupDataProvider<*>> = listOf(\n"
        providers.forEach { provider ->
            outputStream += "\tm${provider.providerClassName},\n"
        }
        outputStream += ")"

        outputStream += "\n\n"

        providers.forEach { provider ->
            outputStream += "@Deprecated(\n\tmessage = \"Generated extensions will be removed in v2.x.x, using Mockup.get() as replacement.\",\n\treplaceWith = ReplaceWith(expression = \"Mockup.get<${provider.providerClassName}>()\"),\n)\n"
            outputStream += "public val Mockup.${provider.providerClassName.decapitalized()}: ${provider.providerClassName}\n"
            outputStream += "\tget() = m${provider.providerClassName}\n"
            outputStream += "\n\n"
        }

    }
}