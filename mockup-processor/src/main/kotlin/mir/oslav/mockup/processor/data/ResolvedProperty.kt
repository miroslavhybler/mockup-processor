@file:Suppress("RedundantConstructorKeyword")

package mir.oslav.mockup.processor.data

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Representing class property
 * @param name Name of the property from class (e.g. id, name, ...), name of the property's type
 * is in [resolvedType]
 * @param type Raw [KSType] of property
 * @param declaration Original property declaration. May be null when property is declared in primary
 * constructor. See [primaryConstructorDeclaration]
 * @param primaryConstructorDeclaration <b>Added in 1.1.6</b>, non null when property of class is
 * declared in it's primary constructor, which is most common case for data classes.
 * @param resolvedType Wrapped type of this property
 * @param isMutable True when property is mutable (declared with var keyword in source). False
 * when property is not mutable (declared with "val" keyword in source)
 * @param isInPrimaryConstructorProperty True when property is declared in primary constructor
 * @param isDelegated True when property is delegated using `by` delegation, false otherwise. Added
 * in 1.2.2.
 * @param containingClassDeclaration Name of the class where is this property declared and used
 * of the class.
 * @since 1.0.0
 */
data class ResolvedProperty constructor(
    val name: String,
    val type: KSType,
    val declaration: KSDeclaration?,
    val primaryConstructorDeclaration: KSValueParameter?,
    val resolvedType: MockupType<*>,
    val isMutable: Boolean,
    val isInPrimaryConstructorProperty: Boolean,
    val isDelegated : Boolean,
    val containingClassDeclaration: KSClassDeclaration,
) {

    /**
     * True when this property is <b>not</b> declared in primary constructor, false otherwise.
     * @see isInPrimaryConstructorProperty
     * @since 1.0.0
     */
    val isNotInPrimaryConstructorProperty: Boolean
        get() = !isInPrimaryConstructorProperty


    /**
     * True when this property is not delegated, false otherwise.
     * @since 1.2.2
     */
    val isNotDelegate: Boolean
        get() = !isDelegated


    /**
     * Returns name of class which is containing this property.
     */
    val containingClassName: String
        get() = containingClassDeclaration.simpleName.asString()
}
