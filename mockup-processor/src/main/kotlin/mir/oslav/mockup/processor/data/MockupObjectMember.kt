package mir.oslav.mockup.processor.data


/**
 * Holds temporary data for property which will be included in generated Mockup.kt object.
 * @param providerClassName Class name (type) of mockup data provider providing data for the type.
 * @param providerClassPackage Package name of generated provider.
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 18.09.2023
 */
data class MockupObjectMember constructor(
    val providerClassName: String,
    val providerClassPackage: String,
    val parentQualifiedName: String?
) {

val qualifiedName: String
    get() = "${providerClassPackage}.${providerClassName}"



}