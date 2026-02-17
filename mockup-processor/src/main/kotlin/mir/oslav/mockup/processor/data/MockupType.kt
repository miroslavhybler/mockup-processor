@file:Suppress("RedundantConstructorKeyword")

package mir.oslav.mockup.processor.data

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.mockup.annotations.Mockup


/**
 * Wrapper class around [KSType] containing additional data
 * @param name Name of type class or property name based on context
 * @param type Resolved [KSType]
 * @param declaration
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 21.09.2023
 */
sealed class MockupType<out D : KSDeclaration> private constructor(
    open val name: String,
    open val providerName: String,
    open val type: KSType,
    open val declaration: D
) {


    /**
     * Package name of the [declaration]
     * @since 1.0.0
     */
    val packageName: String get() = declaration.packageName.asString()


    /**
     * Represents a simple data type (etc. [Int], [String], ...), [KSType.isSimpleType] must always
     * be true, otherwise type was not recognized correctly and [WrongTypeException] would be thrown
     * elsewhere.
     * @see KSType.isSimpleType
     * @since 1.0.0
     */
    data class Simple constructor(
        override val name: String,
        override val type: KSType,
        override val declaration: KSDeclaration,
        val property: KSPropertyDeclaration,
        val source: Source<*>,
    ) : MockupType<KSDeclaration>(
        name = name,
        providerName = name,
        type = type,
        declaration = declaration
    ) {


        /**
         * TODO docs
         * @since 1.2.2
         */
        sealed class Source<T : Any> private constructor() {


            /**
             * @since 1.2.2
             */
            sealed class IntNumber : Source<IntNumber>() {

                /**
                 * @since 1.2.2
                 */
                data class Range constructor(val from: Int, val to: Int) : IntNumber()

                /**
                 * @since 1.2.2
                 */
                data class Def constructor(val values: List<Int>) : IntNumber()

                /**
                 * @since 1.2.2
                 */
                data object Random : IntNumber()
            }


            /**
             * @since 1.2.2
             */
            sealed class FloatNumber : Source<FloatNumber>() {

                /**
                 * @since 1.2.2
                 */
                data class Range constructor(val from: Float, val to: Float) : FloatNumber()


                /**
                 * @since 1.2.2
                 */
                data object Random : FloatNumber()
            }


            /**
             * @since 1.2.2
             */
            sealed class Text : Source<String>() {

                /**
                 * @since 1.2.2
                 */
                data class Def constructor(val values: List<String>) : Text()


                /**
                 * @since 1.2.2
                 */
                data object Random : Text()
            }


            /**
             * Other Primitive types does not have annotation related limitations, so generic object is used
             * to avoid nullability.
             * @since 1.2.2
             */
            data object Random : Source<Nothing>()
        }

    }

    /**
     * Representing type for classes annotated with @[Mockup] annotation.
     * @since 1.0.0
     */
    data class MockUpped constructor(
        override val name: String,
        override val providerName: String,
        override val type: KSType,
        override val declaration: KSClassDeclaration,
        val data: MockupAnnotationData,
        val imports: List<String>,
        val properties: List<ResolvedProperty>
    ) : MockupType<KSClassDeclaration>(
        name = name,
        providerName = providerName,
        type = type,
        declaration = declaration
    )

    /**
     * Represents enum type.
     * @since 1.0.0
     */
    data class Enum constructor(
        override val name: String,
        override val providerName: String,
        override val type: KSType,
        override val declaration: KSDeclaration,
        val enumEntries: List<KSDeclaration>
    ) : MockupType<KSDeclaration>(
        name = name,
        providerName = providerName,
        type = type,
        declaration = declaration,
    )


    /**
     * Represents generic collection type,  [KSType.isGenericCollectionType] must be true for [type]
     * @param elementType Resolved [MockupType] of elements,
     * @since 1.0.0
     */
    data class Collection constructor(
        override val name: String,
        override val type: KSType,
        override val declaration: KSClassDeclaration,
        val elementType: MockupType<*>,
        val imports: List<String>,
    ) : MockupType<KSClassDeclaration>(
        name = name,
        providerName = name,
        type = type,
        declaration = declaration
    ) {

    }

    /**
     * Represents a array with known type (e.g. intArray, floatArray, ...).
     * @see KSType.isFixedArrayType
     * @since 1.0.0
     */
    data class FixedTypeArray constructor(
        override val name: String,
        override val type: KSType,
        override val declaration: KSDeclaration,
    ) : MockupType<KSDeclaration>(
        name = name,
        providerName = name,
        type = type,
        declaration = declaration
    )
}