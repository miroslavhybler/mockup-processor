package mir.oslav.mockup.processor.data

/**
 * Compile-time description of a generated provider that should be mentioned in runtime
 * diagnostics when `Mockup.get<T>()` cannot resolve an erased generic/typealias type.
 *
 * @property rawClassName Fully qualified raw class name visible after JVM erasure.
 * @property targetTypeName Concrete generated target type name.
 * @property providerClassName Simple generated provider class name.
 * @property providerClassPackage Package containing the generated provider and accessor.
 * @property accessorName Generated `Mockup` extension property name.
 * @since 2.0.0
 */
data class MockupProviderHintData constructor(
    val rawClassName: String,
    val targetTypeName: String,
    val providerClassName: String,
    val providerClassPackage: String,
    val accessorName: String,
) {

    /**
     * Fully qualified generated provider class name.
     * @since 2.0.0
     */
    val qualifiedProviderClassName: String
        get() = "$providerClassPackage.$providerClassName"

    /**
     * Fully qualified import for the generated accessor property.
     * @since 2.0.0
     */
    val accessorImport: String
        get() = "$providerClassPackage.$accessorName"
}
