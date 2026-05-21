package mir.oslav.mockup.processor.generation

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType


/**
 * True if this type is [Short] number, false otherwise
 * @since 1.0.0
 */
val KSType.isShort: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Short"


/**
 * True if this type is [Int] number, false otherwise
 * @since 1.0.0
 */
val KSType.isInt: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Int"


/**
 * True if this type is [Long] number, false otherwise
 * @since 1.0.0
 */
val KSType.isLong: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Long"


/**
 * True if this type is [Float] number, false otherwise
 * @since 1.0.0
 */
val KSType.isFloat: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Float"


/**
 * True if this type is [Double] number, false otherwise
 * @since 1.0.0
 */
val KSType.isDouble: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Double"


/**
 * True if this type is [Boolean], false otherwise
 * @since 1.0.0
 */
val KSType.isBoolean: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Boolean"


/**
 * True if this type is [Byte], false otherwise
 * @since 1.0.0
 */
val KSType.isByte: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Byte"


/**
 * True if this type is [String], false otherwise
 * @since 1.0.0
 */
val KSType.isString: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.String"


/**
 * True if this type is [List], false otherwise
 * @since 1.0.0
 */
val KSType.isList: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.collections.List"


/**
 * True if this type is [ArrayList], false otherwise
 * @since 1.0.0
 */
val KSType.isArrayList: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.collections.ArrayList"

/**
 * True if this type is [Array], false otherwise
 * @since 1.0.0
 */
val KSType.isArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.Array"


/**
 * True if this type is [ShortArray], false otherwise
 * @since 1.0.0
 */
val KSType.isShortArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.ShortArray"


/**
 * True if this type is [IntArray], false otherwise
 * @since 1.0.0
 */
val KSType.isIntArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.IntArray"


/**
 * True if this type is [LongArray], false otherwise
 * @since 1.0.0
 */
val KSType.isLongArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.LongArray"


/**
 * True if this type is [FloatArray], false otherwise
 * @since 1.0.0
 */
val KSType.isFloatArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.FloatArray"


/**
 * True if this type is [DoubleArray], false otherwise
 * @since 1.0.0
 */
val KSType.isDoubleArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.DoubleArray"


/**
 * True if this type is [CharArray], false otherwise
 * @since 1.0.0
 */
val KSType.isCharArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.CharArray"


/**
 * True if this type is [ByteArray], false otherwise
 * @since 1.0.0
 */
val KSType.isByteArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.ByteArray"


/**
 * True if this type is [BooleanArray], false otherwise
 * @since 1.0.0
 */
val KSType.isBooleanArray: Boolean
    get() = declaration.qualifiedName?.asString() == "kotlin.BooleanArray"


/**
 * True if this type is simple kotlin language type, false otherwise
 * @since 1.0.0
 */
val KSType.isSimpleType: Boolean
    get() = this.isShort
            || this.isInt
            || this.isLong
            || this.isFloat
            || this.isDouble
            || this.isBoolean
            || this.isByte
            || this.isString


/**
 * True if this type is array with known (not generic) type, false otherwise
 * @since 1.0.0
 */
val KSType.isFixedArrayType: Boolean
    get() = this.isShortArray
            || this.isIntArray
            || this.isLongArray
            || this.isFloatArray
            || this.isDoubleArray
            || this.isBooleanArray
            || this.isByteArray


/**
 * True if this type is collection with generic parameter, false otherwise. Arraylist check added
 * at version 1.1.0
 * @since 1.0.0
 */
val KSType.isGenericCollectionType: Boolean
    get() = this.isArray
            || this.isList


/**
 * True if this type's declaration is Enum
 * @since 1.1.7
 */
val KSType.isEnumType: Boolean
    get() {
        val classDeclaration = declaration as? KSClassDeclaration ?: return false
        return classDeclaration.classKind == ClassKind.ENUM_CLASS
    }
