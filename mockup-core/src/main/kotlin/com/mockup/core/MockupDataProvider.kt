package com.mockup.core


import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlin.reflect.KClass

/**
 * Defines the mockup data provider class.
 * For more information visit [Github repository](https://github.com/miroslavhybler/ksp-mockup)
 * Report issue [here](https://github.com/miroslavhybler/ksp-mockup/issues)
 * @param values Generated mockup data, must be not empty
 * @author Miroslav Hýbler <br>
 * created on 19.12.2025
 * @since 2.0.O
 */
public abstract class MockupDataProvider<T : Any> constructor(
    override val values: Sequence<T> = emptySequence(),
    val clazz: KClass<T>,
) : PreviewParameterProvider<T> {

    /**
     * Returns the first element from the [values] [Sequence].
     * @since 2.0.O
     */
    @Deprecated(message = "use first()")
    val single: T get() = values.first()


    /**
     * Returns the first element from the [values] [Sequence].
     * @since 2.0.O
     */
    val first: T get() = values.first()


    /**
     * Returns [values] as [List].
     * @since 2.0.O
     */
    val list: List<T> get() = values.toList()


    /**
     * Returns a random element from the [values] [Sequence].
     * @since 2.0.O
     */
    val random: T get() = list.random()


    /**
     * Returns the number of elements in the [values] [Sequence].
     * @since 2.0.O
     */
    override val count: Int
        get() = values.count()

}