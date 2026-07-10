package mir.oslav.mockup.processor

import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.StringDef
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Variance
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
class MockupVisitor constructor(
    private val environment: SymbolProcessorEnvironment,
    private val resolver: Resolver,
    private val outputTypeList: ArrayList<MockupType<*>>,
    private val allClassesDeclarations: List<KSClassDeclaration>
) : KSVisitorVoid() {

    /**
     * Type-parameter bindings that are currently known while resolving a generic mockup template.
     * @since 2.0.0
     */
    private data class TypeContext constructor(
        val bindings: Map<KSDeclaration, KSType> = emptyMap(),
    )

    /**
     * Stack of declarations currently being resolved. It is used to stop circular mockup graphs
     * with a readable processor error instead of allowing recursive property resolution to overflow
     * the stack.
     * @since 2.0.0
     */
    private val classResolutionStack: ArrayDeque<KSClassDeclaration> = ArrayDeque()

    /**
     * Visits class annotated with [Mockup] and resolves it's properties.
     * @since 1.0.0
     */
    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) {
        if (classDeclaration.typeParameters.isNotEmpty()) {
            environment.logger.warn(
                "@Mockup class ${classDeclaration.qualifiedName?.asString()} has generic type " +
                        "parameters and will be used as a mockup template only. Create a concrete " +
                        "property type or typealias to generate data for it.",
                classDeclaration,
            )
            return
        }

        val resolvedProperties: ArrayList<ResolvedProperty> = ArrayList()

        visitClassImpl(
            classDeclaration = classDeclaration,
            outputList = resolvedProperties,
            typeContext = TypeContext(),
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
            type = classType,
            data = annotationData,
            declaration = classDeclaration,
            parentDeclarations = parents,
        )

        outputTypeList.add(element = mockupClass)
    }

    /**
     * Visits a concrete typealias whose expanded type points to a class annotated with [Mockup].
     * @since 2.0.0
     */
    fun visitTypeAlias(
        typeAlias: KSTypeAlias,
    ) {
        val aliasedType = typeAlias.type.resolve()
        val classDeclaration = aliasedType.declaration as? KSClassDeclaration ?: return
        val mockupClassDeclaration = allClassesDeclarations.find { mockupClass ->
            mockupClass.qualifiedName == classDeclaration.qualifiedName
        } ?: return

        if (aliasedType.hasUnsupportedGenericArgument()) {
            environment.logger.warn(
                "Skipping typealias ${typeAlias.qualifiedName?.asString()} because its target " +
                        "type contains star projections or unresolved generic parameters. " +
                        "Only concrete @Mockup typealiases are supported.",
                typeAlias,
            )
            return
        }

        val resolvedProperties: ArrayList<ResolvedProperty> = ArrayList()
        val typeContext = createTypeContext(
            classDeclaration = mockupClassDeclaration,
            concreteType = aliasedType,
            parentContext = TypeContext(),
        )

        visitClassImpl(
            classDeclaration = mockupClassDeclaration,
            outputList = resolvedProperties,
            typeContext = typeContext,
        )

        val aliasName = typeAlias.name.getShortName()
        val mockupClass = MockupType.MockUpped(
            name = aliasName,
            providerName = aliasName,
            properties = resolvedProperties,
            type = aliasedType,
            data = visitMockupAnnotation(classDeclaration = mockupClassDeclaration),
            declaration = mockupClassDeclaration,
            parentDeclarations = getAllParents(classDeclaration = mockupClassDeclaration),
            typeAlias = typeAlias,
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
        typeContext: TypeContext,
    ) {
        ensureNoCircularDependency(classDeclaration = classDeclaration)
        classResolutionStack.addLast(classDeclaration)

        try {
            visitClassProperties(
                classDeclaration = classDeclaration,
                outputList = outputList,
                typeContext = typeContext,
            )
        } finally {
            classResolutionStack.removeLast()
        }
    }


    /**
     * Resolves declared properties for [classDeclaration] into [outputList].
     * @since 2.0.0
     */
    private fun visitClassProperties(
        classDeclaration: KSClassDeclaration,
        outputList: ArrayList<ResolvedProperty>,
        typeContext: TypeContext,
    ) {
        val primaryConstructor = classDeclaration.primaryConstructor

        classDeclaration.getDeclaredProperties().forEach { property ->
            val name = property.simpleName.getShortName()
            val type = substituteType(
                type = property.type.resolve(),
                typeContext = typeContext,
            )
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
                    val parameterType = substituteType(
                        type = parameter.type.resolve(),
                        typeContext = typeContext,
                    )
                    val parameterQualifiedName = parameterType.declaration.qualifiedName
                    val constructorPropertyName = parameter.name
                    parameterQualifiedName == typeQualifiedName && propertyName == constructorPropertyName
                })

            val isInsidePrimaryConstructor = primaryConstructorParameter != null


            val propertyType = resolveMockupType(
                type = type,
                property = property,
                name = name,
                primaryConstructorDeclaration = primaryConstructorParameter,
                typeContext = typeContext,
            )

            val resolvedProperty = ResolvedProperty(
                resolvedType = propertyType,
                name = name,
                type = type,
                declaration = declaration,
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
     * Throws a descriptive error when resolving [classDeclaration] would revisit a class already on
     * the current property-resolution stack.
     * @throws IllegalStateException when an unsupported circular mockup dependency is detected.
     * @since 2.0.0
     */
    private fun ensureNoCircularDependency(
        classDeclaration: KSClassDeclaration,
    ) {
        if (classDeclaration !in classResolutionStack) {
            return
        }

        val cycle = (classResolutionStack + classDeclaration).joinToString(separator = " -> ") {
            it.qualifiedName?.asString() ?: it.simpleName.asString()
        }
        throw IllegalStateException(
            "Circular @Mockup dependency detected: $cycle. " +
                    "Circular mock data graphs are not supported. Annotate one side with " +
                    "@IgnoreOnMockup or provide a CustomMockupProvider for that type."
        )
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
     * @return Resolved Mockup type
     * @throws IllegalArgumentException
     * @since 1.0.0
     */
    private fun resolveMockupType(
        type: KSType,
        name: String,
        property: KSPropertyDeclaration,
        primaryConstructorDeclaration: KSValueParameter?,
        typeContext: TypeContext,
    ): MockupType<*> {
        val resolvedType = substituteType(
            type = type,
            typeContext = typeContext,
        )
        require(value = !resolvedType.hasUnsupportedGenericArgument()) {
            createUnsupportedGenericTypeMessage(
                type = resolvedType,
                property = property,
            )
        }

        val declaration = resolvedType.declaration
        return when {
            resolvedType.isSimpleType -> {
                val source = provideSourceForSimpleType(
                    type = resolvedType,
                    propertyDeclaration = property,
                    primaryConstructorDeclaration = primaryConstructorDeclaration,
                )
                MockupType.Simple(
                    name = name,
                    type = resolvedType,
                    declaration = declaration,
                    property = property,
                    source = source,
                )
            }

            resolvedType.isEnumType -> {
                val providerName = createProviderName(declaration as KSClassDeclaration)
                MockupType.Enum(
                    name = name,
                    providerName = providerName,
                    type = resolvedType,
                    declaration = declaration,
                    enumEntries = getEnumConstants(enumType = resolvedType)
                )
            }

            resolvedType.isGenericCollectionType -> {
                val itemType = resolvedType.arguments.lastOrNull()?.type?.resolve()
                    ?: throw IllegalArgumentException(
                        "Unable to resolve generic collection element for property " +
                                "${property.simpleName.asString()}. Star projections are not supported."
                    )

                MockupType.Collection(
                    name = name,
                    type = resolvedType,
                    declaration = declaration as KSClassDeclaration,
                    elementType = resolveMockupType(
                        type = itemType,
                        name = itemType.declaration.simpleName.getShortName(),
                        property = property,
                        primaryConstructorDeclaration = primaryConstructorDeclaration,
                        typeContext = typeContext,
                    ),
                )
            }

            resolvedType.isFixedArrayType -> {
                MockupType.FixedTypeArray(name = name, type = resolvedType, declaration = declaration)
            }

            else -> findMockupClass(
                type = resolvedType,
                typeContext = typeContext,
            )

        }
    }


    /**
     * @return [MockupType] representing class annotated with [Mockup] annotation, null otherwise.
     * @since 1.0.0
     */
    private fun findMockupClass(
        type: KSType,
        typeContext: TypeContext,
    ): MockupType.MockUpped {
        val resolvedType = substituteType(
            type = type,
            typeContext = typeContext,
        )
        val classDeclaration = allClassesDeclarations
            .find(predicate = { mockupClass ->
                mockupClass.qualifiedName == resolvedType.declaration.qualifiedName
            })

        require(
            value = classDeclaration != null,
            lazyMessage = {
                val typeName = resolvedType.declaration.simpleName.getShortName()
                val qualifiedName = resolvedType.declaration.qualifiedName?.asString()
                    ?: resolvedType.declaration.simpleName.asString()
                "Unable to resolve type ${qualifiedName}. This can have two causes:\n" +
                        "Cause 1: Class $typeName is not supported. List of supported types can be found here https://github.com/miroslavhybler/ksp-mockup/#supported-types\n" +
                        "Cause 2: Class $typeName is not annotated with @Mockup annotation.\n" +
                        "If neither of these one has happened, please report an issue here https://github.com/miroslavhybler/ksp-mockup/issues.\n\n"
            }
        )

        val outputPropertiesList: ArrayList<ResolvedProperty> = ArrayList()
        val nestedTypeContext = createTypeContext(
            classDeclaration = classDeclaration,
            concreteType = resolvedType,
            parentContext = typeContext,
        )

        visitClassImpl(
            classDeclaration = classDeclaration,
            outputList = outputPropertiesList,
            typeContext = nestedTypeContext,
        )
        val providerName = createProviderName(classDeclaration)
        val parents = getAllParents(classDeclaration)

        return MockupType.MockUpped(
            name = classDeclaration.simpleName.getShortName(),
            providerName = providerName,
            declaration = classDeclaration,
            parentDeclarations = parents,
            data = visitMockupAnnotation(classDeclaration = classDeclaration),
            type = resolvedType,
            properties = outputPropertiesList
        )
    }

    /**
     * Creates a child context by binding [classDeclaration]'s type parameters to [concreteType]'s
     * arguments.
     * @since 2.0.0
     */
    private fun createTypeContext(
        classDeclaration: KSClassDeclaration,
        concreteType: KSType,
        parentContext: TypeContext,
    ): TypeContext {
        if (classDeclaration.typeParameters.isEmpty()) {
            return parentContext
        }

        require(value = concreteType.arguments.size == classDeclaration.typeParameters.size) {
            "Unable to resolve generic mockup type ${classDeclaration.qualifiedName?.asString()}. " +
                    "Generic @Mockup classes require concrete type arguments from a property or typealias."
        }

        val bindings = parentContext.bindings.toMutableMap()
        classDeclaration.typeParameters.zip(concreteType.arguments).forEach { (parameter, argument) ->
            require(value = argument.variance != Variance.STAR && argument.type != null) {
                "Unable to resolve generic mockup type ${classDeclaration.qualifiedName?.asString()}. " +
                        "Star projections are not supported."
            }

            val argumentType = substituteType(
                type = argument.type!!.resolve(),
                typeContext = parentContext,
            )

            require(value = !argumentType.hasUnsupportedGenericArgument()) {
                "Unable to resolve generic type parameter ${parameter.name.asString()} in " +
                        "${classDeclaration.qualifiedName?.asString()}. Generic @Mockup classes " +
                        "require concrete type arguments from a property or typealias."
            }

            bindings[parameter] = argumentType
        }

        return TypeContext(bindings = bindings)
    }

    /**
     * Replaces type-parameter occurrences in [type] using the current [typeContext].
     * @since 2.0.0
     */
    private fun substituteType(
        type: KSType,
        typeContext: TypeContext,
    ): KSType {
        val replacement = typeContext.bindings[type.declaration]
        if (replacement != null) {
            return if (type.isMarkedNullable) replacement.makeNullable() else replacement
        }
        if (type.arguments.isEmpty()) {
            return type
        }

        val substitutedArguments = type.arguments.map { argument ->
            if (argument.variance == Variance.STAR || argument.type == null) {
                argument
            } else {
                val substitutedType = substituteType(
                    type = argument.type!!.resolve(),
                    typeContext = typeContext,
                )
                resolver.getTypeArgument(
                    typeRef = resolver.createKSTypeReferenceFromKSType(substitutedType),
                    variance = argument.variance,
                )
            }
        }

        return type.replace(arguments = substitutedArguments)
    }

    /**
     * True when this type still contains a type parameter or star projection after substitution.
     * @since 2.0.0
     */
    private fun KSType.hasUnsupportedGenericArgument(): Boolean {
        return unsupportedGenericDescription() != null
    }

    /**
     * Describes the first unsupported generic argument found in this type.
     * @since 2.0.0
     */
    private fun KSType.unsupportedGenericDescription(): String? {
        val typeParameter = declaration as? KSTypeParameter
        if (typeParameter != null) {
            return "type parameter ${typeParameter.name.asString()}"
        }

        arguments.forEach { argument ->
            if (argument.variance == Variance.STAR || argument.type == null) {
                return "star projection"
            }

            argument.type?.resolve()?.unsupportedGenericDescription()?.let { description ->
                return description
            }
        }

        return null
    }

    /**
     * Builds a readable processor failure for unsupported generic properties.
     * @since 2.0.0
     */
    private fun createUnsupportedGenericTypeMessage(
        type: KSType,
        property: KSPropertyDeclaration,
    ): String {
        val unsupportedGeneric = type.unsupportedGenericDescription() ?: "generic type"
        return "Unable to resolve $unsupportedGeneric for property " +
                "${property.simpleName.asString()}. Generic @Mockup classes require concrete " +
                "type arguments from a property or typealias."
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
