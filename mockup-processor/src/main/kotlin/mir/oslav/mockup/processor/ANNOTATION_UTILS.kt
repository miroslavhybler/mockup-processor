package mir.oslav.mockup.processor

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import kotlin.reflect.KClass


/**
 * Finds [target] on this symbol or on one of this symbol's annotations.
 *
 * This supports declarations such as custom annotations annotated with `@IntRange`, `@StringDef`,
 * and similar Android annotations. Direct annotations win over meta-annotations.
 * @param target Annotation class to find.
 * @return Matching [KSAnnotation], or `null` when neither the symbol nor its annotations contain it.
 * @since 1.2.2
 */
fun KSAnnotated.findAnnotationInAnnotationTree(
    target: KClass<*>,
): KSAnnotation? {
    if (this.annotations.count() == 0) {
        return null
    }

    val firstLevelAnnotation = this.annotations
        .find(predicate = { annotation ->
            annotation.isInstanceOf(target = target)
        })

    if (firstLevelAnnotation != null) {
        //This property is annotated by target itself
        return firstLevelAnnotation
    }

    //This is annotation that is annotated by the target
    val annotated: KSAnnotation = this.annotations
        .find(
            predicate = { annotation ->
                annotation.isAnnotatedWith(target = target)
            }
        ) ?: return null

    //Resolved annotation type
    val annotatedType = annotated.annotationType.resolve()
    //Actual declaration of the annotation holding the annotations of the found annotation
    val annotatedDeclaration = annotatedType.declaration

    val targetAnnotation = annotatedDeclaration.findAnnotationInstance(target = target)
    return targetAnnotation
}


/**
 * Tries to find annotation by [target] in [KSAnnotated.annotations].
 * @param target Annotation class to find directly on this symbol.
 * @return Matching [KSAnnotation], or `null` when it is not present.
 * @since 1.2.2
 */
fun KSAnnotated.findAnnotationInstance(
    target: KClass<*>,
): KSAnnotation? {
    return this.annotations.find(predicate = { annotation ->
        annotation.isInstanceOf(target = target)
    })
}


/**
 * Checks whether this annotation declaration is itself annotated with [target].
 * @param target Meta-annotation class to find.
 * @return `true` when [target] is present on this annotation's declaration.
 * @since 1.2.2
 */
fun KSAnnotation.isAnnotatedWith(
    target: KClass<*>,
): Boolean {
    val annotationType = this.annotationType.resolve()
    val annotationDeclaration = annotationType.declaration

    return annotationDeclaration.annotations.any(
        predicate = { annotation ->
            annotation.isInstanceOf(target = target)
        }
    )
}


/**
 * Checks whether this [KSAnnotation] resolves to [target].
 * @param target Annotation class to compare against.
 * @return `true` when this annotation's qualified name matches [target].
 * @since 1.2.2
 */
fun KSAnnotation.isInstanceOf(target: KClass<*>): Boolean {
    val qualifiedName = this.annotationType.resolve()
        .declaration.qualifiedName?.asString()
    return qualifiedName == target.qualifiedName
}
