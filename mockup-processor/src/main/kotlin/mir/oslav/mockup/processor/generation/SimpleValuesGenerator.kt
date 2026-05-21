package mir.oslav.mockup.processor.generation

import com.squareup.kotlinpoet.CodeBlock
import mir.oslav.mockup.processor.data.MockupType
import mir.oslav.mockup.processor.data.ResolvedProperty
import mir.oslav.mockup.processor.data.WrongTypeException
import mir.oslav.mockup.processor.data.loremIpsumWords
import kotlin.random.Random


/**
 * TODO - handle properties with lazy declaration (do not generate value)
 * @author Miroslav Hýbler <br>
 * created on 17.05.2024
 * @since 1.1.6
 */
class SimpleValuesGenerator constructor() {

    /**
     * Generates random code for value of [property], e.g. `id = 123`
     * @param property Single property of class
     * @return Generated code
     * @throws WrongTypeException
     * @since 1.1.6
     */
    fun generate(
        property: MockupType.Simple,
        resolvedProperty: ResolvedProperty,
    ): CodeBlock {
        val type = property.type
        return when {
            //Simple types and string
            type.isShort -> {
                CodeBlock.of(
                    "%L",
                    Random.nextInt(
                        from = Short.MIN_VALUE.toInt(),
                        until = Short.MAX_VALUE.toInt(),
                    ),
                )
            }

            type.isInt -> {
                generateIntegerValue(
                    property = property,
                    resolvedProperty = resolvedProperty,
                )
            }

            type.isFloat -> {
                generateFloatValue(
                    property = property,
                    resolvedProperty = resolvedProperty,
                )
            }

            type.isLong -> CodeBlock.of("%L", Random.nextInt())
            type.isDouble -> CodeBlock.of("%L", Random.nextDouble())
            type.isBoolean -> CodeBlock.of("%L", Random.nextBoolean())
            type.isString -> {
                generateStringValue(
                    property = property,
                    resolvedProperty = resolvedProperty,
                )
            }

            else -> throw WrongTypeException(expectedType = "Simple", givenType = "$type")
        }
    }


    /**
     * ### Refactored in 1.2.2
     * [MockupType.Simple.Source.IntNumber] was introduced to handle values and annotations based limitations.
     * @param property
     * @param resolvedProperty
     * @return Generated code consisting of string holding generated int value
     * @since 1.1.6
     */
    private fun generateIntegerValue(
        property: MockupType.Simple,
        resolvedProperty: ResolvedProperty
    ): CodeBlock {

        val source = property.source as? MockupType.Simple.Source.IntNumber
            ?: throw WrongTypeException(
                expectedType = "IntNumber",
                givenType = "${property.source}"
            )


        return when (source) {
            is MockupType.Simple.Source.IntNumber.Range -> {
                val intValue = Random.nextInt(from = source.from, until = source.to)
                CodeBlock.of("%L", intValue)
            }

            is MockupType.Simple.Source.IntNumber.Def -> {
                val intValue = source.values.random()
                CodeBlock.of("%L", intValue)
            }

            is MockupType.Simple.Source.IntNumber.Random -> {
                val intValue = Random.nextInt()
                CodeBlock.of("%L", intValue)
            }
        }
    }


    /**
     * @param property
     * @param resolvedProperty
     * @return Generated code consisting of string holding generated float value
     * @since 1.1.6
     */
    private fun generateFloatValue(
        property: MockupType.Simple,
        resolvedProperty: ResolvedProperty
    ): CodeBlock {
        val source = property.source as? MockupType.Simple.Source.FloatNumber
            ?: throw WrongTypeException(
                expectedType = "FloatNumber",
                givenType = "${property.source}"
            )

        return when (source) {
            is MockupType.Simple.Source.FloatNumber.Range -> {
                val floatValue = Random.nextFloat()
                CodeBlock.of("%Lf", floatValue)
            }

            is MockupType.Simple.Source.FloatNumber.Random -> {
                val floatValue = Random.nextFloat()
                CodeBlock.of("%Lf", floatValue)
            }
        }
    }


    fun generateStringValue(
        property: MockupType.Simple,
        resolvedProperty: ResolvedProperty
    ): CodeBlock {
        val source = property.source as? MockupType.Simple.Source.Text
            ?: throw WrongTypeException(
                expectedType = "Text",
                givenType = "${property.source}"
            )

        return when (source) {
            is MockupType.Simple.Source.Text.Def -> {
                val stringValue = source.values.random()
                CodeBlock.of("%S", stringValue)
            }

            is MockupType.Simple.Source.Text.Random -> {
                val loremIpsum = loremIpsumWords(wordCount = Random.nextInt(from = 2, until = 60))
                CodeBlock.of("%S", loremIpsum)
            }
        }
    }
}
