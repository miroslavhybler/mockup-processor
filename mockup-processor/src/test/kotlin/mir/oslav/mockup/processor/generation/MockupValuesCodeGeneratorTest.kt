package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import mir.oslav.mockup.processor.data.MockupAnnotationData
import mir.oslav.mockup.processor.data.MockupType
import mir.oslav.mockup.processor.data.ResolvedProperty
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class MockupValuesCodeGeneratorTest {

    @Test
    fun listOfStringsWithRecognizedImageNameGeneratesList() {
        val stringType = kSType(qualifiedName = "kotlin.String")
        val listType = kSType(qualifiedName = "kotlin.collections.List")
        val property = resolvedProperty(
            name = "galleryImageModels",
            type = listType,
            resolvedType = MockupType.Collection(
                name = "galleryImageModels",
                type = listType,
                declaration = listType.declaration as KSClassDeclaration,
                elementType = simpleStringType(
                    name = "galleryImageModels",
                    type = stringType,
                ),
            ),
        )
        val mockupClass = mockupClass(
            qualifiedName = "com.test.PaintingDetailUiModel",
            properties = listOf(property),
        )

        val generatedCode = MockupValuesCodeGenerator()
            .generate(mockupClass = mockupClass, mockupClasses = listOf(mockupClass))
            .toString()

        assertTrue(generatedCode.contains("galleryImageModels = listOf("))
        assertFalse(generatedCode.contains("galleryImageModels = \"https://"))
    }

    @Test
    fun listOfMockedClassesGeneratesList() {
        val attachmentClass = mockupClass(
            qualifiedName = "com.test.Attachment",
            properties = listOf(
                resolvedProperty(
                    name = "path",
                    type = kSType(qualifiedName = "kotlin.String"),
                    resolvedType = simpleStringType(
                        name = "path",
                        type = kSType(qualifiedName = "kotlin.String"),
                    ),
                    containingClassDeclaration = kSClassDeclaration("com.test.Attachment"),
                )
            ),
        )
        val listType = kSType(qualifiedName = "kotlin.collections.List")
        val property = resolvedProperty(
            name = "attachments",
            type = listType,
            resolvedType = MockupType.Collection(
                name = "attachments",
                type = listType,
                declaration = listType.declaration as KSClassDeclaration,
                elementType = attachmentClass,
            ),
        )
        val mockupClass = mockupClass(
            qualifiedName = "com.test.Product",
            properties = listOf(property),
        )

        val generatedCode = MockupValuesCodeGenerator()
            .generate(mockupClass = mockupClass, mockupClasses = listOf(mockupClass, attachmentClass))
            .toString()

        assertTrue(generatedCode.contains("attachments = listOf("))
        assertTrue(generatedCode.contains("com.test.Attachment("))
    }

    private fun mockupClass(
        qualifiedName: String,
        properties: List<ResolvedProperty>,
    ): MockupType.MockUpped {
        val declaration = kSClassDeclaration(qualifiedName = qualifiedName)
        return MockupType.MockUpped(
            name = qualifiedName.substringAfterLast('.'),
            providerName = qualifiedName.substringAfterLast('.'),
            type = kSType(qualifiedName = qualifiedName, declaration = declaration),
            declaration = declaration,
            parentDeclarations = emptyList(),
            data = MockupAnnotationData(
                count = 1,
                name = "",
                enableNullValues = false,
            ),
            properties = properties,
        )
    }

    private fun resolvedProperty(
        name: String,
        type: KSType,
        resolvedType: MockupType<*>,
        containingClassDeclaration: KSClassDeclaration = kSClassDeclaration("com.test.Container"),
    ): ResolvedProperty {
        return ResolvedProperty(
            name = name,
            type = type,
            declaration = type.declaration,
            primaryConstructorDeclaration = null,
            resolvedType = resolvedType,
            isMutable = false,
            isInPrimaryConstructorProperty = true,
            isDelegated = false,
            containingClassDeclaration = containingClassDeclaration,
        )
    }

    private fun simpleStringType(
        name: String,
        type: KSType,
    ): MockupType.Simple {
        return MockupType.Simple(
            name = name,
            type = type,
            declaration = type.declaration,
            property = kSPropertyDeclaration(name = name),
            source = MockupType.Simple.Source.Text.Random,
        )
    }

    private fun kSType(
        qualifiedName: String,
        declaration: KSClassDeclaration = kSClassDeclaration(qualifiedName = qualifiedName),
    ): KSType {
        lateinit var type: KSType
        type = proxy { method, _ ->
            when (method.name) {
                "getDeclaration" -> declaration
                "getNullability" -> Nullability.NOT_NULL
                "getArguments" -> emptyList<Any>()
                "getAnnotations" -> emptySequence<KSAnnotation>()
                "isMarkedNullable" -> false
                "isError" -> false
                "isFunctionType" -> false
                "isSuspendFunctionType" -> false
                "makeNullable" -> type
                "makeNotNullable" -> type
                "starProjection" -> type
                "replace" -> type
                else -> unhandled(method = method)
            }
        }
        return type
    }

    private fun kSClassDeclaration(
        qualifiedName: String,
    ): KSClassDeclaration {
        val simpleName = qualifiedName.substringAfterLast('.')
        val packageName = qualifiedName.substringBeforeLast('.', missingDelimiterValue = "")
        return proxy { method, _ ->
            when (method.name) {
                "getSimpleName" -> kSName(simpleName)
                "getQualifiedName" -> kSName(qualifiedName)
                "getPackageName" -> kSName(packageName)
                "getParentDeclaration" -> null
                "getTypeParameters" -> emptyList<Any>()
                "getAnnotations" -> emptySequence<KSAnnotation>()
                "getContainingFile" -> null
                else -> unhandled(method = method)
            }
        }
    }

    private fun kSPropertyDeclaration(
        name: String,
    ): KSPropertyDeclaration {
        return proxy { method, _ ->
            when (method.name) {
                "getSimpleName" -> kSName(name)
                "getQualifiedName" -> kSName(name)
                "getPackageName" -> kSName("")
                "getParentDeclaration" -> null
                "getTypeParameters" -> emptyList<Any>()
                "getAnnotations" -> emptySequence<KSAnnotation>()
                "getContainingFile" -> null
                else -> unhandled(method = method)
            }
        }
    }

    private fun kSName(value: String): KSName {
        return proxy { method, _ ->
            when (method.name) {
                "asString" -> value
                "getQualifier" -> value.substringBeforeLast('.', missingDelimiterValue = "")
                "getShortName" -> value.substringAfterLast('.')
                else -> unhandled(method = method)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Any> proxy(
        crossinline handler: (Method, Array<Any?>?) -> Any?,
    ): T {
        return Proxy.newProxyInstance(
            T::class.java.classLoader,
            arrayOf(T::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "toString" -> "${T::class.java.simpleName}Proxy"
                "hashCode" -> System.identityHashCode(proxy)
                "equals" -> proxy === args?.firstOrNull()
                else -> handler(method, args) ?: defaultValue(method.returnType)
            }
        } as T
    }

    private fun defaultValue(type: Class<*>): Any? {
        return when {
            type == Boolean::class.javaPrimitiveType -> false
            type == Int::class.javaPrimitiveType -> 0
            type == Long::class.javaPrimitiveType -> 0L
            type == Float::class.javaPrimitiveType -> 0f
            type == Double::class.javaPrimitiveType -> 0.0
            type == Void.TYPE -> Unit
            List::class.java.isAssignableFrom(type) -> emptyList<Any>()
            Set::class.java.isAssignableFrom(type) -> emptySet<Any>()
            Sequence::class.java.isAssignableFrom(type) -> emptySequence<Any>()
            else -> null
        }
    }

    private fun unhandled(method: Method): Nothing? = null
}
