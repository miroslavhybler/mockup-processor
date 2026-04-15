package mir.oslav.mockup.processor.data


/**
 * Holds input options from Gradle. If they are not inputted, they are replaced with default properties.
 * You can set custom input properties like this:
 * ```kotlin
 * ksp {<br>
 *    arg(k = "mockup.dateFormat", v = "yyyy-MM-dd")
 *    arg(k= "usePreviewParameterProviders", v = true)
 * }
 * ```
 * @since 1.1.0
 * @author Miroslav Hýbler <br>
 * created on 16.11.2023
 */
data class InputOptions constructor(
    val defaultDateFormat: String,
    val usePreviewParameterProviders: Boolean = false,
) {

}
