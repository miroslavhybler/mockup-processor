package mir.oslav.mockup.processor.data


/**
 * Holds temporary data for property which will be included in generated Mockup.kt object.
 * @param providerClassName Class name (type) of mockup data provider providing data for the type.
 * @param providerClassPackage Package name of generated provider.
 * @param providerHint Optional runtime diagnostic hint for providers that cannot be discovered
 * through erased `Mockup.get<T>()` lookup.
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 18.09.2023
 */
data class MockupObjectMember constructor(
    val providerClassName: String,
    val providerClassPackage: String,
    val isGetApiReplacementAvailable: Boolean = true,
    val providerHint: MockupProviderHintData? = null,
) {

    /**
     * Fully qualified generated provider class name.
     * @since 1.0.0
     */
    val qualifiedName: String
        get() = "${providerClassPackage}.${providerClassName}"
}
