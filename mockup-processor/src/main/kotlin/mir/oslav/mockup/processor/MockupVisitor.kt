package mir.oslav.mockup.processor

import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.StringDef
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.mockup.annotations.IgnoreOnMockup
import com.mockup.annotations.Mockup
import mir.oslav.mockup.processor.data.MockupAnnotationData
import mir.oslav.mockup.processor.data.MockupType
import mir.oslav.mockup.processor.data.ResolvedProperty
import mir.oslav.mockup.processor.generation.isEnumEntry
import mir.oslav.mockup.processor.generation.isEnumType
import mir.oslav.mockup.processor.generation.isFixedArrayType
import mir.oslav.mockup.processor.generation.isFloat
import mir.oslav.mockup.processor.generation.isGenericCollectionType
import mir.oslav.mockup.processor.generation.isInt
import mir.oslav.mockup.processor.generation.isSimpleType
import mir.oslav.mockup.processor.generation.isString


/**
 *
 * @param environment
 * @param outputTypeList Output List where resolved types will be stored.
 * @param allClassesDeclarations Input list containing declarations from target module of all classes
 * annotated with [Mockup] annotation.
 * @since 1.0.0
 * @author Miroslav Hýbler <br>
 * created on 15.09.2023
 */
//TODO circular dependency - when class uses itself as parameter it leads to stackOverflow
class MockupVisitor constructor(
    private val environment: SymbolProcessorEnvironment,
    private val outputTypeList: ArrayList<MockupType<*>>,
    private val allClassesDeclarations: List<KSClassDeclaration>
) : KSVisitorVoid() {


    /**
     * @since 1.0.0
     */
    var imports: List<String> = emptyList()


    /**
     * Visits class annotated with [Mockup] and resolves it's properties.
     * @since 1.0.0
     */
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        val resolvedProperties: ArrayList<ResolvedProperty> = ArrayList()

        visitClassImpl(
            classDeclaration = classDeclaration,
            outputList = resolvedProperties
        )

        val annotationData = visitMockupAnnotation(classDeclaration = classDeclaration)
        val classType = classDeclaration.asType(typeArguments = emptyList())
        val providerName = createProviderName(classDeclaration = classDeclaration)
        val parents = getAllParents(classDeclaration = classDeclaration)
        val mockupClass = MockupType.MockUpped(
            name = annotationData.name.takeIf(predicate = String::isNotBlank)
                ?: classDeclaration.simpleName.getShortName(),
            providerName = providerName,
            properties = resolvedProperties,
            imports = imports,
            type = classType,
            data = annotationData,
            declaration = classDeclaration,
            parentDeclarations = parents,
        )

        outputTypeList.add(element = mockupClass)
    }


    /**
     * Visits class annotated with [Mockup] and resolves it's properties. Properties will be inserted
     * into [outputList]
     * @param classDeclaration Declaration of class
     * @param outputList Output list where resolved properties will be added
     * @since 1.0.0
     */
    private fun visitClassImpl(
        classDeclaration: KSClassDeclaration,
        outputList: ArrayList<ResolvedProperty>,
    ) {
        val primaryConstructor = classDeclaration.primaryConstructor

        classDeclaration.getDeclaredProperties().forEach { property ->
            val name = property.simpleName.getShortName()
            val type = property.type.resolve()
            val declaration = type.declaration
            val annotations = property.annotations

            val foundAnnotation = annotations.find(predicate = { annotation ->
                val declaration = annotation.annotationType.resolve().declaration
                val qualifiedName = declaration.qualifiedName?.asString()
                qualifiedName == IgnoreOnMockup::class.qualifiedName
            })

            if (foundAnnotation != null) {
                //Skipping because property is annotated with @IgnoreOnMockup, meaning that it should be ignored
                return@forEach
            }

            val typeQualifiedName = type.declaration.qualifiedName
            val propertyName = property.simpleName
            val primaryConstructorParameter = primaryConstructor?.parameters
                ?.find(predicate = { parameter ->
                    val parameterType = parameter.type.resolve()
                    val parameterQualifiedName = parameterType.declaration.qualifiedName
                    val constructorPropertyName = parameter.name
                    parameterQualifiedName == typeQualifiedName && propertyName == constructorPropertyName
                })

            val isInsidePrimaryConstructor = primaryConstructorParameter != null


            val propertyType = resolveMockupType(
                type = type,
                property = property,
                name = name,
                imports = imports,
                primaryConstructorDeclaration = primaryConstructorParameter,
            )

            val resolvedProperty = ResolvedProperty(
                resolvedType = propertyType,
                name = name,
                type = type,
                declaration = declaration,
                imports = imports,
                isMutable = property.isMutable,
                isDelegated = property.isDelegated(),
                isInPrimaryConstructorProperty = isInsidePrimaryConstructor,
                containingClassDeclaration = classDeclaration,
                primaryConstructorDeclaration = primaryConstructorParameter
            )
            outputList.add(element = resolvedProperty)
        }
    }


    /**
     * Visits [classDeclaration] and tries to extract [Mockup] annotation data.
     * @param classDeclaration Class declaration. Should be ALWAYS annotated with [Mockup].
     * @throws IllegalStateException If class is not annotated with [Mockup] annotations. This should
     * never happen since classes are queried by [MockupProcessor.findAnnotatedClasses] which takes
     * classes ONLY annotated with [Mockup]. If this happens  please report an issue
     * <a href="https://github.com/miroslavhybler/ksp-mockup/issues">here</a>.
     * @throws TypeCastException When [Mockup] annotation data would be invalid. This should never
     * happen but if so, please report an issue <a href="https://github.com/miroslavhybler/ksp-mockup/issues">here</a>.
     * @return Extracted [Mockup] annotation data.
     * @since 1.0.0
     */
    private fun visitMockupAnnotation(
        classDeclaration: KSClassDeclaration
    ): MockupAnnotationData {
        val annotation = classDeclaration.annotations
            .find(predicate = { ksAnnotation ->
                val declaration = ksAnnotation.annotationType.resolve().declaration
                val qualifiedName = declaration.qualifiedName?.asString()
                qualifiedName == Mockup::class.qualifiedName
            })

        require(
            value = annotation != null,
            lazyMessage = {
                "Unable to resolve type, class ${classDeclaration.qualifiedName?.asString()} " +
                        "is probably not annotated with @Mockup! If your class is annotated please " +
                        "report an issue here https://github.com/miroslavhybler/ksp-mockup/issues."
            }
        )


        var count = 10
        var enableNullValues = false
        var name = ""

        annotation.arguments.forEach { argument ->
            when (argument.name?.getShortName()) {
                "count" -> count = argument.value as Int
                "enableNullValues" -> enableNullValues = argument.value as Boolean
                "name" -> name = argument.value as String
            }
        }

        return MockupAnnotationData(
            count = count,
            name = name,
            enableNullValues = enableNullValues
        )
    }


    /**
     * Tries to resolve [type]
     * @param type -> Type to resolve
     * @param name -> name of type class or property name based on context
     * @param property -> Property declaration with [type]
     * @param imports -> Imports that are needed by [type] and [property]
     * @return Resolved Mockup type
     * @throws IllegalArgumentException
     * @since 1.0.0
     */
    private fun resolveMockupType(
        type: KSType,
        name: String,
        property: KSPropertyDeclaration,
        imports: List<String>,
        primaryConstructorDeclaration: KSValueParameter?,
    ): MockupType<*> {
        val declaration = type.declaration
        return when {
            type.isSimpleType -> {
                val source = provideSourceForSimpleType(
                    type = type,
                    propertyDeclaration = property,
                    primaryConstructorDeclaration = primaryConstructorDeclaration,
                )
                MockupType.Simple(
                    name = name,
                    type = type,
                    declaration = declaration,
                    property = property,
                    source = source,
                )
            }

            type.isEnumType -> {
                //Since enums doesn't need @Mockup annotation it's required to include import manually
                this.imports += listOf(type.declaration.qualifiedName!!.asString())
                val providerName = createProviderName(declaration as KSClassDeclaration)
                MockupType.Enum(
                    name = name,
                    providerName = providerName,
                    type = type,
                    declaration = declaration,
                    enumEntries = getEnumConstants(enumType = type)
                )
            }

            type.isGenericCollectionType -> {
                val itemType = property.type.element?.typeArguments?.lastOrNull()?.type?.resolve()
                    ?: throw NullPointerException("")

                MockupType.Collection(
                    name = name,
                    type = type,
                    declaration = declaration as KSClassDeclaration,
                    elementType = resolveMockupType(
                        type = itemType,
                        name = declaration.simpleName.getShortName(),
                        imports = imports,
                        property = property,
                        primaryConstructorDeclaration = primaryConstructorDeclaration,
                    ),
                    imports = imports
                )
            }

            type.isFixedArrayType -> {
                MockupType.FixedTypeArray(name = name, type = type, declaration = declaration)
            }

            else -> findMockupClass(type = type)

        }
    }


    /**
     * @return [MockupType] representing class annotated with [Mockup] annotation, null otherwise.
     * @since 1.0.0
     */
    private fun findMockupClass(
        type: KSType
    ): MockupType.MockUpped {
        val classDeclaration = allClassesDeclarations
            .find(predicate = { mockupClass ->
                mockupClass.qualifiedName == type.declaration.qualifiedName
            })

        require(
            value = classDeclaration != null,
            lazyMessage = {
                val typeName = type.declaration.simpleName.getShortName()
                val qualifiedName = type.declaration.qualifiedName!!.asString()
                "Unable to resolve type ${qualifiedName}. This can have two causes:\n" +
                        "Cause 1: Class $typeName is not supported. List of supported types can be found here https://github.com/miroslavhybler/ksp-mockup/#supported-types\n" +
                        "Cause 2: Class $typeName is not annotated with @Mockup annotation.\n" +
                        "If neither of these one has happened, please report an issue here https://github.com/miroslavhybler/ksp-mockup/issues.\n\n"
            }
        )

        val outputPropertiesList: ArrayList<ResolvedProperty> = ArrayList()

        visitClassImpl(
            classDeclaration = classDeclaration,
            outputList = outputPropertiesList,
        )
        val providerName = createProviderName(classDeclaration)
        val parents = getAllParents(classDeclaration)

        return MockupType.MockUpped(
            name = classDeclaration.simpleName.getShortName(),
            providerName = providerName,
            declaration = classDeclaration,
            parentDeclarations = parents,
            data = visitMockupAnnotation(classDeclaration = classDeclaration),
            type = type,
            imports = imports,
            properties = outputPropertiesList
        )
    }


    /**
     * @return List of enum constants (similar to `Enum.entries`)
     * @since 1.1.7
     */
    private fun getEnumConstants(
        enumType: KSType
    ): List<KSDeclaration> {
        require(value = enumType.isEnumType) {
            "To read enum entries provided type has to be enum!!"
        }
        val classDeclaration = enumType.declaration as? KSClassDeclaration ?: return emptyList()
        val entries = classDeclaration.declarations
            .filter(predicate = KSDeclaration::isEnumEntry)
            .toList()
        return entries
    }


    /**
     * @since 1.2.2
     */
    private fun provideSourceForSimpleType(
        type: KSType,
        propertyDeclaration: KSAnnotated,
        primaryConstructorDeclaration: KSValueParameter?,
    ): MockupType.Simple.Source<*> {
        return when {
            type.isInt -> {
                getIntSource(
                    propertyDeclaration = propertyDeclaration,
                    primaryConstructorDeclaration = primaryConstructorDeclaration,
                )
            }

            type.isFloat -> {
                getFloatSource(
                    propertyDeclaration = propertyDeclaration,
                    primaryConstructorDeclaration = primaryConstructorDeclaration,
                )
            }

            type.isString -> {
                getStringSource(
                    propertyDeclaration = propertyDeclaration,
                    primaryConstructorDeclaration = primaryConstructorDeclaration,
                )
            }

            else -> MockupType.Simple.Source.Random
        }
    }


    /**
     * @since 1.2.2
     */
    private fun getIntSource(
        propertyDeclaration: KSAnnotated,
        primaryConstructorDeclaration: KSValueParameter?,
    ): MockupType.Simple.Source.IntNumber {
        primaryConstructorDeclaration?.findAnnotationInAnnotationTree(target = IntRange::class)
            ?.let { intRangeAnnotation ->
                val range = Annotations.processRangeAnnotation(
                    annotation = intRangeAnnotation,
                    min = Int.MIN_VALUE,
                    max = Int.MAX_VALUE,
                )

                return MockupType.Simple.Source.IntNumber.Range(
                    from = range.first,
                    to = range.second,
                )
            }
        propertyDeclaration.findAnnotationInAnnotationTree(target = IntRange::class)
            ?.let { intRangeAnnotation ->
                val range = Annotations.processRangeAnnotation(
                    annotation = intRangeAnnotation,
                    min = Int.MIN_VALUE,
                    max = Int.MAX_VALUE,
                )

                return MockupType.Simple.Source.IntNumber.Range(
                    from = range.first,
                    to = range.second,
                )
            }
        primaryConstructorDeclaration?.findAnnotationInAnnotationTree(target = IntDef::class)
            ?.let { intRangeAnnotation ->
                val range = Annotations.processRangeAnnotation(
                    annotation = intRangeAnnotation,
                    min = Int.MIN_VALUE,
                    max = Int.MAX_VALUE,
                )

                return MockupType.Simple.Source.IntNumber.Range(
                    from = range.first,
                    to = range.second,
                )
            }
        propertyDeclaration.findAnnotationInAnnotationTree(target = IntDef::class)
            ?.let { intDefAnnotation ->
                val possibleValues: List<Int> = Annotations.proccessDefAnnotation(
                    annotation = intDefAnnotation,
                )
                return if (possibleValues.isNotEmpty()) {
                    MockupType.Simple.Source.IntNumber.Def(
                        values = possibleValues
                    )
                } else {
                    MockupType.Simple.Source.IntNumber.Random
                }
            }

        return MockupType.Simple.Source.IntNumber.Random
    }


    /**
     * @since 1.2.2
     */
    private fun getFloatSource(
        propertyDeclaration: KSAnnotated,
        primaryConstructorDeclaration: KSValueParameter?,
    ): MockupType.Simple.Source.FloatNumber {
        primaryConstructorDeclaration?.findAnnotationInAnnotationTree(target = FloatRange::class)
            ?.let { floatRangeAnnotation ->
                val range = Annotations.processRangeAnnotation(
                    annotation = floatRangeAnnotation,
                    min = Float.MIN_VALUE,
                    max = Float.MAX_VALUE,
                )

                return MockupType.Simple.Source.FloatNumber.Range(
                    from = range.first,
                    to = range.second,
                )
            }
        propertyDeclaration.findAnnotationInAnnotationTree(target = FloatRange::class)
            ?.let { floatRangeAnnotation ->
                val range = Annotations.processRangeAnnotation(
                    annotation = floatRangeAnnotation,
                    min = Float.MIN_VALUE,
                    max = Float.MAX_VALUE,
                )

                return MockupType.Simple.Source.FloatNumber.Range(
                    from = range.first,
                    to = range.second,
                )
            }
        return MockupType.Simple.Source.FloatNumber.Random
    }


    /**
     * @since 1.2.2
     */
    private fun getStringSource(
        propertyDeclaration: KSAnnotated,
        primaryConstructorDeclaration: KSValueParameter?,
    ): MockupType.Simple.Source.Text {
        primaryConstructorDeclaration?.findAnnotationInAnnotationTree(target = StringDef::class)
            ?.let { stringDefAnnotation ->
                val possibleValues: List<String> = Annotations.proccessDefAnnotation(
                    annotation = stringDefAnnotation,
                )
                return if (possibleValues.isNotEmpty()) {
                    MockupType.Simple.Source.Text.Def(
                        values = possibleValues
                    )
                } else {
                    MockupType.Simple.Source.Text.Random
                }
            }

        propertyDeclaration.findAnnotationInAnnotationTree(target = StringDef::class)
            ?.let { stringDefAnnotation ->
                val possibleValues: List<String> = Annotations.proccessDefAnnotation(
                    annotation = stringDefAnnotation,
                )
                return if (possibleValues.isNotEmpty()) {
                    MockupType.Simple.Source.Text.Def(
                        values = possibleValues
                    )
                } else {
                    MockupType.Simple.Source.Text.Random
                }
            }

        return MockupType.Simple.Source.Text.Random
    }


    private fun createProviderName(
        classDeclaration: KSClassDeclaration,
    ): String {
        var parent = classDeclaration.parentDeclaration as? KSClassDeclaration
        var name = classDeclaration.simpleName.getShortName()
        while (parent != null) {
            name = parent.simpleName.getShortName() + name
            parent = parent.parentDeclaration as? KSClassDeclaration
        }
        return name
    }

    private fun getAllParents(
        classDeclaration: KSClassDeclaration,
    ): List<KSDeclaration> {
        val parents = mutableListOf<KSDeclaration>()
        var parent = classDeclaration.parentDeclaration
        while (parent != null) {
            parents.add(parent)
            parent = (parent as? KSClassDeclaration)?.parentDeclaration
        }
        return parents
    }
}