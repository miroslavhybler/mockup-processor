package com.mockup.exampledata

import android.annotation.SuppressLint
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.StringDef
import com.mockup.annotations.IgnoreOnMockup
import com.mockup.annotations.Mockup
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Locale


/**
 * @param rating Example usage of FloatRange annotation
 * @param readersCount Example usage of IntRange annotation, number of people who read the article
 * @author Miroslav Hýbler <br>
 * created on 15.09.2023
 */
@Mockup
data class Article constructor(
    val id: Int,
    val title: String,
    val content: String,
    val author: Publisher,
    val tags: Array<String>,
    val categories: List<Category>,
    val isSpecialEdition: Boolean,
    val imageUrl: String,
    val gallery: List<GalleryPhoto>,
    val createdAt: String,
    @ArticleType
    val type: Int,
    @FloatRange(from = 0.0, to = 5.0)
    val rating: Float,
    @IntRange(from = 0, to = 5_000)
    val readersCount: Int,
    val minutesReading: Short,
) {

    @IgnoreOnMockup
    val topReader: Reader? = null

    companion object {
        //You can set custom dateTime format for the date generation in your app's build.gradle.kts
        //ksp {
        //  arg(k = "mockup-date-format", v = "yyyy-MM-dd")
        //}
        //https://www.joda.org/joda-time/key_format.html
        @SuppressLint("ConstantLocale") // for simplification
        private val dateParser: SimpleDateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss Z",
            Locale.getDefault(),
        )

        @SuppressLint("ConstantLocale") // for simplification
        private val dateFormatter: SimpleDateFormat = SimpleDateFormat(
            "dd. MM. yyyy",
            Locale.getDefault(),
        )
    }


    val createdAtFormatted: String by lazy {  dateFormatter.format(dateParser.parse(createdAt)!!) }


    @IntDef(
        ArticleType.regular,
        ArticleType.silver,
        ArticleType.gold,
    )
    @Retention(value = AnnotationRetention.BINARY)
    @Target(AnnotationTarget.PROPERTY)
    annotation class ArticleType {
        companion object {
            const val regular: Int = 0
            const val silver: Int = 1
            const val gold: Int = 2
        }
    }


    @Mockup
    data class GalleryPhoto constructor(
        val imageUrl: String,
        @PhotoType
        val type: String,
    ) {
        @StringDef(
            PhotoType.REGULAR,
            PhotoType.HEADER,
        )
        annotation class PhotoType {
            companion object {
                const val REGULAR: String = "REGULAR"
                const val HEADER: String = "HEADER"
            }
        }

    }
}

@Mockup
data class Category constructor(
    val id: Int,
    val name: String,
    @ColorInt
    val color: Int
) {

    val formattedName: String
        get() {
            return if (name.length > 11) name.take(n = 11) else name
        }
}


@Mockup(count = 3)
class Publisher constructor() {
    var id: Int = 0
    var firstName: String = "John"
    var lastName: String = "Doe"
    var dateOfBirth: String = "01-01-1970"

    var description: String = ""
    var themeImageUrl: String? = null
    var avatarUrl: String? = null

    @IntRange(from = 0, to = 1_000)
    var articlesCount: Int = 0

    //We want to exclude this because mockup library can't generate it
    @IgnoreOnMockup
    var someUnknownObject: Any? = null

    var authorRank: AuthorRank = AuthorRank.GOLD

    val fullName: String get() = "$firstName $lastName"
}


enum class AuthorRank {
    GOLD, SILVER, BRONZE;
}



/**
 * Example for Json serialization (requires `org.jetbrains.kotlin.plugin.serialization` plugin)
 */
@Serializable
class Reader constructor(
    @SerialName(value = "userName")
    val userName: UserName,
) {

    val surname: String
        get() = userName.surname
    val birthname: String
        get() = userName.birthname


    @Serializable
    data class UserName constructor(
        @SerialName(value = "surname")
        val surname: String,
        @SerialName(value = "birthname")
        val birthname: String
    ) {

    }
}