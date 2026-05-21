package mir.oslav.mockup.processor.generation

import com.mockup.annotations.IgnoreOnMockup
import com.squareup.kotlinpoet.CodeBlock
import mir.oslav.mockup.processor.data.MockupType
import mir.oslav.mockup.processor.data.ResolvedProperty
import mir.oslav.mockup.processor.data.WrongTypeException
import mir.oslav.mockup.processor.recognition.BaseRecognizer
import mir.oslav.mockup.processor.recognition.DateTimeRecognizer
import mir.oslav.mockup.processor.recognition.ImageUrlRecognizer
import mir.oslav.mockup.processor.recognition.UsernameRecognizer
import kotlin.random.Random

/**
 * Generates the `Sequence<T>` expression passed into generated mockup data providers.
 * @param simpleValuesGenerator Generator used for scalar literals.
 * @param recognizers Contextual value generators such as image URL and date recognizers.
 * @since 2.0.0
 */
class MockupValuesCodeGenerator constructor(
    private val simpleValuesGenerator: SimpleValuesGenerator = SimpleValuesGenerator(),
    private val recognizers: List<BaseRecognizer> = listOf(
        ImageUrlRecognizer(),
        DateTimeRecognizer(),
        UsernameRecognizer(),
    ),
) {

    /**
     * Generates a `sequenceOf(...)` expression for [mockupClass].
     * @param mockupClass Mockup class whose instances should be generated.
     * @param mockupClasses All mockup classes known in the current processor run.
     * @return Code block containing the provider values expression.
     * @since 2.0.0
     */
    fun generate(
        mockupClass: MockupType.MockUpped,
        mockupClasses: List<MockupType.MockUpped>,
    ): CodeBlock {
        return CodeBlock.builder()
            .add("sequenceOf(\n")
            .indent()
            .apply {
                repeat(times = mockupClass.data.count) {
                    add(
                        "%L,\n",
                        generateCodeForMockUppedType(
                            type = mockupClass,
                            mockupClasses = mockupClasses,
                        )
                    )
                }
            }
            .unindent()
            .add(")")
            .build()
    }

    /**
     * Generates the right-hand side expression for [property].
     */
    private fun generateCodeForProperty(
        property: ResolvedProperty,
        mockupClasses: List<MockupType.MockUpped>,
    ): CodeBlock {
        recognizers.forEach { recognizer ->
            val recognizedValueCode = recognizer.tryRecognizeAndGenerateCodeBlock(
                property = property,
                containingClassName = property.containingClassName,
            )
            if (recognizedValueCode != null) {
                return recognizedValueCode
            }
        }

        return when (val type = property.resolvedType) {
            is MockupType.Simple -> simpleValuesGenerator.generate(
                property = type,
                resolvedProperty = property,
            )

            is MockupType.MockUpped -> generateCodeForMockUppedType(
                type = type,
                mockupClasses = mockupClasses,
            )

            is MockupType.Enum -> CodeBlock.of(
                "%T.%L",
                type.toClassName(),
                type.enumEntries.random().simpleName.asString(),
            )

            is MockupType.Collection -> generateCollectionValueCode(
                type = type,
                property = property,
                mockupClasses = mockupClasses,
            )

            is MockupType.FixedTypeArray -> generateCodeForFixedTypeArray(type = type)
        }
    }

    /**
     * Generates `listOf(...)` or `arrayOf(...)` for a collection [type].
     */
    private fun generateCollectionValueCode(
        type: MockupType.Collection,
        property: ResolvedProperty,
        mockupClasses: List<MockupType.MockUpped>,
    ): CodeBlock {
        val factoryName = when {
            type.type.isList -> "listOf"
            type.type.isArray -> "arrayOf"
            else -> throw WrongTypeException(
                expectedType = "Generic collection type",
                givenType = type.name,
            )
        }

        return CodeBlock.builder()
            .add("%L(\n", factoryName)
            .indent()
            .apply {
                when (val elementType = type.elementType) {
                    is MockupType.Simple -> {
                        repeat(times = Random.nextInt(from = 1, until = 6)) {
                            add(
                                "%L,\n",
                                simpleValuesGenerator.generate(
                                    property = elementType,
                                    resolvedProperty = property,
                                )
                            )
                        }
                    }

                    is MockupType.MockUpped -> {
                        repeat(times = 5) {
                            add(
                                "%L,\n",
                                generateCodeForMockUppedType(
                                    type = elementType,
                                    mockupClasses = mockupClasses,
                                )
                            )
                        }
                    }

                    is MockupType.Enum -> {
                        add(
                            "%T.%L,\n",
                            elementType.toClassName(),
                            elementType.enumEntries.random().simpleName.asString(),
                        )
                    }

                    is MockupType.FixedTypeArray -> {
                        add("%L,\n", generateCodeForFixedTypeArray(type = elementType))
                    }

                    is MockupType.Collection -> Unit
                }
            }
            .unindent()
            .add(")")
            .build()
    }

    /**
     * Generates an empty primitive-array expression for [type].
     */
    private fun generateCodeForFixedTypeArray(
        type: MockupType.FixedTypeArray,
    ): CodeBlock {
        val elementType = type.type
        return when {
            elementType.isShortArray -> CodeBlock.of("shortArrayOf()")
            elementType.isIntArray -> CodeBlock.of("intArrayOf()")
            elementType.isLongArray -> CodeBlock.of("longArrayOf()")
            elementType.isFloatArray -> CodeBlock.of("floatArrayOf()")
            elementType.isDoubleArray -> CodeBlock.of("doubleArrayOf()")
            elementType.isCharArray -> CodeBlock.of("charArrayOf()")
            elementType.isByteArray -> CodeBlock.of("byteArrayOf()")
            elementType.isBooleanArray -> CodeBlock.of("booleanArrayOf()")
            else -> throw WrongTypeException(
                expectedType = "FixedArrayType",
                givenType = "$elementType",
            )
        }
    }

    /**
     * Generates a constructor/apply expression for a nested mocked type.
     */
    private fun generateCodeForMockUppedType(
        type: MockupType.MockUpped,
        mockupClasses: List<MockupType.MockUpped>,
    ): CodeBlock {
        val declaration = type.type.declaration
        val memberClassName = declaration.simpleName.getShortName()
        val memberClassPackageName = declaration.packageName.asString()

        val memberClass = mockupClasses.find { mockupClass ->
            mockupClass.declaration == declaration &&
                    mockupClass.packageName == memberClassPackageName
        } ?: throw NullPointerException(
            "Cannot generate mockup data for class ${memberClassName}. This can have two causes:\n" +
                    "Cause 1: Class $memberClassName is not supported. List of supported types can be found here https://github.com/miroslavhybler/ksp-mockup/#supported-types\n" +
                    "Cause 2: Class $memberClassName is not annotated with @Mockup annotation.\n" +
                    "If you want to exclude it, use @IgnoreOnMockup annotation on the parameter.\n" +
                    "If neither of these one has happened, please report an issue here https://github.com/miroslavhybler/ksp-mockup/issues.\n\n"
        )

        return CodeBlock.builder()
            .add(
                "%L",
                generateItemPrimaryConstructorCall(
                    mockupClass = memberClass,
                    mockupClasses = mockupClasses,
                )
            )
            .apply {
                generateItemApplyCall(
                    mockupClass = memberClass,
                    mockupClasses = mockupClasses,
                )?.let { applyCode ->
                    add("%L", applyCode)
                }
            }
            .build()
    }

    /**
     * Generates the constructor call for [mockupClass].
     */
    private fun generateItemPrimaryConstructorCall(
        mockupClass: MockupType.MockUpped,
        mockupClasses: List<MockupType.MockUpped>,
    ): CodeBlock {
        val typeName = mockupClass.toClassName()
        val constructorProperties = mockupClass.properties
            .filter(predicate = ResolvedProperty::isInPrimaryConstructorProperty)

        if (constructorProperties.isEmpty()) {
            return CodeBlock.of("%T()", typeName)
        }

        return CodeBlock.builder()
            .add("%T(\n", typeName)
            .indent()
            .apply {
                constructorProperties.forEach { property ->
                    add(
                        "%L = %L,\n",
                        property.name.decapitalized(),
                        generateCodeForProperty(
                            property = property,
                            mockupClasses = mockupClasses,
                        )
                    )
                }
            }
            .unindent()
            .add(")")
            .build()
    }

    /**
     * Generates the `.apply { ... }` block for mutable properties outside the primary constructor.
     */
    private fun generateItemApplyCall(
        mockupClass: MockupType.MockUpped,
        mockupClasses: List<MockupType.MockUpped>,
    ): CodeBlock? {
        val notConstructorParameters = mockupClass.properties
            .filter(predicate = ResolvedProperty::isMutable)
            .filter(predicate = ResolvedProperty::isNotDelegate)
            .filter(predicate = ResolvedProperty::isNotInPrimaryConstructorProperty)
            .filterNot { property -> property.isIgnoredOnMockup() }

        if (notConstructorParameters.isEmpty()) {
            return null
        }

        return CodeBlock.builder()
            .add(".apply {\n")
            .indent()
            .apply {
                notConstructorParameters.forEach { property ->
                    add(
                        "%L = %L\n",
                        property.name.decapitalized(),
                        generateCodeForProperty(
                            property = property,
                            mockupClasses = mockupClasses,
                        )
                    )
                }
            }
            .unindent()
            .add("}")
            .build()
    }

    /**
     * Returns true when this property is annotated with `@IgnoreOnMockup`.
     */
    private fun ResolvedProperty.isIgnoredOnMockup(): Boolean {
        return type.annotations.find { annotation ->
            val declaration = annotation.annotationType.resolve().declaration
            declaration.qualifiedName?.asString() == IgnoreOnMockup::class.qualifiedName
        } != null
    }
}
