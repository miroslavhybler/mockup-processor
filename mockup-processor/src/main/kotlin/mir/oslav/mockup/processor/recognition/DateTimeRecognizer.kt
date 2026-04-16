package mir.oslav.mockup.processor.recognition

import mir.oslav.mockup.processor.MockupProcessor
import mir.oslav.mockup.processor.data.InputOptions
import mir.oslav.mockup.processor.data.ResolvedProperty
import mir.oslav.mockup.processor.generation.isInt
import mir.oslav.mockup.processor.generation.isLong
import mir.oslav.mockup.processor.generation.isString
import mir.oslav.mockup.processor.recognition.DateTimeRecognizer.Companion.recognizableNames
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random


/**
 * Recognizer for date and time, using [recognizableNames]. Can generate Int, Long and String values. <br>
 * You can set custom dateTime format like this:
 * ```kotlin
 * ksp {<br>
 *    arg(k = "mockup-date-format", v = "yyyy-MM-dd")<br>
 * }
 * ```
 * @since 1.1.0
 * @author Miroslav Hýbler <br>
 * created on 16.11.2023
 */
class DateTimeRecognizer constructor() : BaseRecognizer() {

    companion object {

        /**
         * Default format used when mockup-date-format is not set
         * @since 1.1.0
         */
        const val defaultFormat: String = "yyyy-MM-dd HH:mm:ssZZ"


        /**
         * @since 1.0.0
         */
        private val recognizableNames: List<String> = listOf(
            "date", "date_time", "dateTime",
            "created_at", "createdAt",
            "updated_at", "updatedAt",
            "deleted_at", "deletedAt",
            "date_of_birth", "dateOfBirth",
            "date_from", "dateFrom",
            "date_to", "dateTo",
            "fromDate", "toDate",
            "from_date", "to_date",
        )
    }


    /**
     * @since 1.1.0
     */
    private val calendar = Calendar.getInstance()


    /**
     * Tries to recognize if [property] is contextually a dateTime based on it's name
     * @return True when [recognizableNames] contains [ResolvedProperty.name] meaning that property is
     * contextually a dateTime.
     * @since 1.1.0
     */
    @Deprecated(message = "Refactor in 1.2.0, recognition and code generation in two steps meains more code and twice as time. Put recognition and generation in one step.")
    override fun recognize(property: ResolvedProperty, containingClassName: String): Boolean {
        return recognizableNames.contains(element = property.name)
    }


    /**
     * Generates code value for recognized [property]. Can generate [Int], [Long] and [String] values,
     * other types are not supported.
     * @since 1.1.0
     */
    @Deprecated(message = "Refactor in 1.2.0, recognition and code generation in two steps means more code and twice as time. Put recognition and generation in one step.")
    override fun generateCodeValueForProperty(property: ResolvedProperty): String {
        val type = property.type
        val code = when {
            type.isLong -> "${System.currentTimeMillis()}"
            type.isInt -> "${System.currentTimeMillis() / 1000L}"
            type.isString -> generateStringDate()
            else -> throw IllegalStateException(
                "Unable to generate dateTime for type" +
                        " ${type.declaration.simpleName} (${type.declaration.qualifiedName})"
            )
        }

        return code
    }


    /**
     * @since 1.2.0
     */
    override fun tryRecognizeAndGenerateValue(
        property: ResolvedProperty,
        containingClassName: String
    ): String? {
        if (recognizableNames.contains(element = property.name)) {
            return generateCodeValueForProperty(property = property)
        }

        return null
    }


    /**
     * Generates random date and formats it by [InputOptions.defaultDateFormat]
     * @return Code for string dateTime property, e.g. 22-05-2023
     * @since 1.1.0
     */
    private fun generateStringDate(): String {
        val options = MockupProcessor.inputOptions
            ?: throw IllegalStateException(
                "Unable to generate date, MockupProcessor.inputOptions are null!!"
            )
        val format = options.defaultDateFormat

        val year = Random.nextInt(from = 1900, until = 2100)
        val month = Random.nextInt(from = 1, until = 12)

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val day = Random.nextInt(from = 1, until = maxDay)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        val hour = Random.nextInt(from = 0, until = 23)
        val minute = Random.nextInt(from = 0, until = 59)
        val second = Random.nextInt(from = 0, until = 59)
        val millis = Random.nextInt(from = 0, until = 999)
        val zone = Random.nextInt(from = -12, until = 14)


        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, second)
        calendar.set(Calendar.MILLISECOND, millis)
        calendar.set(Calendar.ZONE_OFFSET, zone)

        val formatter = try {
            SimpleDateFormat(format, Locale.getDefault())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "DateTimeFormatter was not able to process desired mockup-date-format, " +
                        "check your mockup-date-format argument in app.gradle.kts file. " +
                        "Error: ${e.message}"
            )
        }
        val stringDate = formatter.format(Date(calendar.timeInMillis))
        return "\"$stringDate\""
    }


}