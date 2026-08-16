package mir.oslav.mockup.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.mockup.annotations.Mockup
import mir.oslav.mockup.processor.data.InputOptions
import mir.oslav.mockup.processor.data.MockupObjectMember
import mir.oslav.mockup.processor.data.MockupProviderHintData
import mir.oslav.mockup.processor.data.MockupType
import mir.oslav.mockup.processor.generation.MockupDataProviderGenerator
import mir.oslav.mockup.processor.generation.MockupObjectExtensionGenerator
import mir.oslav.mockup.processor.generation.MockupRegistryGenerator
import mir.oslav.mockup.processor.generation.MockupValuesCodeGenerator
import mir.oslav.mockup.processor.generation.decapitalized
import mir.oslav.mockup.processor.generation.toProviderTargetTypeName
import mir.oslav.mockup.processor.recognition.DateTimeRecognizer
import java.io.OutputStream


/**
 * Processor of ksp-mockup library.
 * @param environment
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 15.09.2023
 */
class MockupProcessor constructor(
    private val environment: SymbolProcessorEnvironment,
) : SymbolProcessor {


    companion object {

        /**
         * Input options for processor that can be passed as arguments using ksp block, e.g.
         * ```kotlin
         * ksp {
         *     arg(k = "mockup-date-format", v = "yyyy-MM-dd")
         * }
         * ```
         * @since 1.1.0
         */
        /**
         * Resolved processor options for the current KSP run.
         * Defaults are available before [process] runs so value recognizers never have to handle
         * a missing options object.
         * @since 1.1.0
         */
        var inputOptions: InputOptions = InputOptions(
            defaultDateFormat = DateTimeRecognizer.defaultFormat,
        )
            private set

        private const val CUSTOM_PROVIDER_QUALIFIED_NAME: String =
            "com.mockup.core.CustomMockupProvider"
    }

    /**
     * List of all classes annotated with [Mockup] annotation and all other found supported types.
     * @since 1.0.0
     */
    private val mockupTypesList: ArrayList<MockupType<*>> = ArrayList()


    /**
     * @since 1.0.0
     */
    private val dataProvidersGenerator: MockupDataProviderGenerator = MockupDataProviderGenerator()


    /**
     * @since 1.0.0
     */
    private lateinit var visitor: MockupVisitor


    /**
     * Generates provider value expressions from resolved mockup metadata.
     * @since 2.0.0
     */
    private val mockupValuesCodeGenerator: MockupValuesCodeGenerator = MockupValuesCodeGenerator()

    /**
     * In order to prevent ksp from <a href="https://kotlinlang.org/docs/ksp-multi-round.html#changes-to-getsymbolsannotatedwith">multiple round processing</a>
     * [process] should be processing only once. When [wasInvoked] is true, [emptyList] is returned
     * immediately from [process].
     * @since 1.0.0
     */
    private var wasInvoked: Boolean = false


    /**
     * @since 1.1.0
     */
    private var generatedProvidersCount: Int = 0


    /**
     * @since 1.0.0
     */
    override fun process(
        resolver: Resolver,
    ): List<KSAnnotated> {
        val dateFormat = environment.options["mockup-date-format"]
            ?: environment.options["mockup.dateFormat"]
            ?: DateTimeRecognizer.defaultFormat
        val usePreviewParameterProviders =
            environment.options["mockup.usePreviewParameterProviders"]
                ?.toBoolean() == true

        inputOptions = InputOptions(
            defaultDateFormat = dateFormat,
            usePreviewParameterProviders = usePreviewParameterProviders,
        )

        if (wasInvoked && generatedProvidersCount > 0) {
            // If processor was invoked previously return emptyList() immediately for unwanted
            // multiple round processing.
            return emptyList()
        }

        val mockupClassDeclarations = resolver.findAnnotatedClasses()
        val mockupTypeAliases = resolver.findMockupTypeAliases(
            mockupClassDeclarations = mockupClassDeclarations,
        )
        val customProviders = resolver.findCustomMockupProviders()

        if (Debugger.isDebugEnabled) {
            try {
                Debugger.setOutputStream(
                    outputStream = environment.codeGenerator.createNewFile(
                        packageName = "com.mockup",
                        fileName = "logs",
                        dependencies = Dependencies(
                            aggregating = false,
                            sources = mockupClassDeclarations
                                .mapNotNull(transform = KSClassDeclaration::containingFile)
                                .toTypedArray()
                        ),
                    )
                )
            } catch (exception: FileAlreadyExistsException) {
                exception.printStackTrace()
                //Do nothing
            }
        }


        mockupTypesList.clear()

        visitor = MockupVisitor(
            environment = environment,
            resolver = resolver,
            outputTypeList = mockupTypesList,
            allClassesDeclarations = mockupClassDeclarations,
        )

        mockupClassDeclarations.forEach { classDeclaration ->
            visitor.visitClassDeclaration(
                classDeclaration = classDeclaration,
                data = Unit,
            )
        }
        mockupTypeAliases.forEach { typeAlias ->
            visitor.visitTypeAlias(typeAlias = typeAlias)
        }

        val providers = generateMockupDataProviders(
            mockupClasses = mockupTypesList.filterIsInstance<MockupType.MockUpped>(),
            sourceDeclarations = mockupClassDeclarations + mockupTypeAliases,
        )

        if (providers.isNotEmpty()) {
            generateMockupRegistry(
                providers = customProviders,
                providerHints = providers.mapNotNull { provider -> provider.providerHint },
                dependenciesSources = mockupClassDeclarations + mockupTypeAliases + customProviders,
            )
        }

        val targetPackage = providers.firstOrNull()?.providerClassPackage

        if (targetPackage != null && providers.isNotEmpty()) {
            MockupObjectExtensionGenerator(
                outputStream = generateOutputFile(
                    declarations = mockupClassDeclarations + mockupTypeAliases,
                    filename = "EXTENSIONS",
                    packageName = targetPackage,
                ),
                targetPackageName = targetPackage
            ).generate(providers = providers)
        }

        generatedProvidersCount = providers.size
        wasInvoked = true

        Debugger.close()
        return emptyList()
    }

    /**
     * Generates the registry that registers all discovered custom providers.
     * @param providers Custom provider declarations found in processed sources.
     * @param providerHints Generated provider hints for erased generic/typealias diagnostics.
     * @param dependenciesSources Source declarations that should invalidate the generated registry.
     * @since 2.0.0
     */
    private fun generateMockupRegistry(
        providers: List<KSClassDeclaration>,
        providerHints: List<MockupProviderHintData>,
        dependenciesSources: List<KSDeclaration>,
    ) {
        val dependencies = Dependencies(
            aggregating = true,
            sources = dependenciesSources
                .mapNotNull { it.containingFile }
                .toTypedArray(),
        )

        try {
            MockupRegistryGenerator(
                outputStream = environment.codeGenerator.createNewFile(
                    packageName = "com.mockup",
                    fileName = "GeneratedMockupRegistry",
                    dependencies = dependencies,
                )
            ).generate(
                providers = providers,
                providerHints = providerHints,
            )
        } catch (exception: FileAlreadyExistsException) {
            exception.printStackTrace()
        }
    }

    /**
     * Finds classes and objects in the module that implement `CustomMockupProvider`.
     * @return Custom provider declarations with valid construction shape.
     * @since 2.0.0
     */
    private fun Resolver.findCustomMockupProviders(): List<KSClassDeclaration> {
        val providers = ArrayList<KSClassDeclaration>()
        getAllFiles().forEach { file ->
            file.declarations.forEach { declaration ->
                val classDeclaration = declaration as? KSClassDeclaration ?: return@forEach
                if (classDeclaration.classKind !in listOf(ClassKind.CLASS, ClassKind.OBJECT)) {
                    return@forEach
                }
                if (classDeclaration.implementsCustomMockupProvider()) {
                    if (classDeclaration.classKind == ClassKind.CLASS) {
                        val hasNoArgConstructor = classDeclaration.primaryConstructor
                            ?.parameters
                            ?.isEmpty() == true
                        if (!hasNoArgConstructor) {
                            environment.logger.error(
                                "CustomMockupProvider ${classDeclaration.qualifiedName?.asString()} must have a no-arg constructor or be an object.",
                                classDeclaration
                            )
                            return@forEach
                        }
                    }
                    providers.add(classDeclaration)
                }
            }
        }
        return providers
    }

    /**
     * Checks whether this declaration implements `CustomMockupProvider` directly or through a
     * superclass/interface chain.
     * @return `true` when the custom provider type is found.
     * @since 2.0.0
     */
    private fun KSClassDeclaration.implementsCustomMockupProvider(): Boolean {
        val visited = HashSet<KSDeclaration>()
        fun visit(declaration: KSDeclaration): Boolean {
            if (!visited.add(declaration)) return false
            val classDecl = declaration as? KSClassDeclaration ?: return false
            classDecl.superTypes.forEach { superType ->
                val resolved = superType.resolve()
                val superDecl = resolved.declaration
                val qualifiedName = superDecl.qualifiedName?.asString()
                if (qualifiedName == CUSTOM_PROVIDER_QUALIFIED_NAME) {
                    return true
                }
                if (visit(superDecl)) {
                    return true
                }
            }
            return false
        }

        return visit(this)
    }


    /**
     * @param classesDeclarations Found declarations of classes annotated with @[Mockup] annotation.
     * @param mockupClasses
     * @return List of [MockupObjectMember]s. These are going to be written into generated Mockup.kt
     * object as public properties for data access.
     * @since 1.0.0
     */
    private fun generateMockupDataProviders(
        sourceDeclarations: List<KSDeclaration>,
        mockupClasses: List<MockupType.MockUpped>,
    ): ArrayList<MockupObjectMember> {

        val outputNamesList = ArrayList<MockupObjectMember>()

        mockupClasses.forEach { mockupClass ->
            val mockupDataGeneratedContent = mockupValuesCodeGenerator.generate(
                mockupClass = mockupClass,
                mockupClasses = mockupClasses,
            )
            val packageName = mockupClass.providerPackageName

            val dataProviderClazzName = dataProvidersGenerator.generateContent(
                outputStream = generateOutputFile(
                    declarations = sourceDeclarations,
                    filename = "${mockupClass.providerName}MockupProvider",
                    packageName = packageName,
                ),
                clazz = mockupClass,
                generatedValuesContent = mockupDataGeneratedContent,
                packageName = packageName,
                usePreviewParameterProviders = inputOptions.usePreviewParameterProviders,
            )
            val member = MockupObjectMember(
                providerClassName = dataProviderClazzName,
                providerClassPackage = packageName,
                isGetApiReplacementAvailable = !mockupClass.requiresGeneratedAccessor,
                providerHint = mockupClass.createProviderHint(
                    providerClassName = dataProviderClazzName,
                    providerClassPackage = packageName,
                ),
            )
            outputNamesList.add(element = member)
        }

        return outputNamesList
    }

    /**
     * Creates runtime diagnostic metadata for generated providers that cannot be discovered through
     * `Mockup.get<T>()` because the JVM only sees the erased raw class.
     */
    private fun MockupType.MockUpped.createProviderHint(
        providerClassName: String,
        providerClassPackage: String,
    ): MockupProviderHintData? {
        if (!requiresGeneratedAccessor) {
            return null
        }

        val rawClassName = declaration.qualifiedName?.asString() ?: return null
        val targetTypeName = typeAlias?.qualifiedName?.asString()
            ?: toProviderTargetTypeName().toString()

        return MockupProviderHintData(
            rawClassName = rawClassName,
            targetTypeName = targetTypeName,
            providerClassName = providerClassName,
            providerClassPackage = providerClassPackage,
            accessorName = providerClassName.decapitalized(),
        )
    }


    /**
     * @return List of declared classes that are annotated with @[Mockup] annotations.
     * @since 1.0.0
     */
    private fun Resolver.findAnnotatedClasses(
    ): List<KSClassDeclaration> = getSymbolsWithAnnotation(
        annotationName = Mockup::class.qualifiedName.toString()
    ).filterIsInstance<KSClassDeclaration>().toList()

    /**
     * Finds typealiases whose resolved target points to one of the classes annotated with [Mockup].
     * @return Typealias declarations that may produce concrete generated providers.
     * @since 2.0.0
     */
    private fun Resolver.findMockupTypeAliases(
        mockupClassDeclarations: List<KSClassDeclaration>,
    ): List<KSTypeAlias> {
        val mockupQualifiedNames = mockupClassDeclarations
            .mapNotNull { declaration -> declaration.qualifiedName?.asString() }
            .toSet()
        if (mockupQualifiedNames.isEmpty()) {
            return emptyList()
        }

        return getAllFiles()
            .flatMap { file -> file.declarations }
            .filterIsInstance<KSTypeAlias>()
            .filter { typeAlias ->
                val aliasedType = typeAlias.type.resolve()
                aliasedType.declaration.qualifiedName?.asString() in mockupQualifiedNames
            }
            .toList()
    }


    /**
     * Creates single file for code generation and returns it's opened [OutputStream]
     * @param filename Filename without *.kt extension
     * @param packageName Package name for generated files
     * @since 1.0.0
     * @throws FileAlreadyExistsException If file already exits
     */
    @Throws(FileAlreadyExistsException::class)
    private fun generateOutputFile(
        declarations: List<KSDeclaration>,
        filename: String,
        packageName: String,
        isAggregating: Boolean = true,
    ): OutputStream {
        return environment.codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = isAggregating,
                sources = declarations
                    .mapNotNull(transform = KSDeclaration::containingFile)
                    .toTypedArray()
            ),
            packageName = packageName,
            fileName = filename.removeSuffix(suffix = ".kt"),
        )
    }
}
