package mir.oslav.mockup.processor.recognition

import com.squareup.kotlinpoet.CodeBlock
import mir.oslav.mockup.processor.data.ResolvedProperty
import mir.oslav.mockup.processor.generation.isString


/**
 * Recognizer for image urls. Mockup data are not meant to be used outside of the compose previews, for
 * authenticity real working image urls are generated.
 * Images are taken from <a href="https://www.pixabay.com/">Pixabay</a>.
 * @since 1.1.0
 * @author Miroslav Hýbler <br>
 * created on 20.10.2023
 */
class ImageUrlRecognizer constructor() : BaseRecognizer() {


    companion object {

        /**
         * @since 1.1.0
         */
        private val recognizableNames: List<String> = listOf(
            "image", "image_url", "imageUrl",
            "picture", "picture_url", "pictureUrl",
            "photo", "photo_url", "photoUrl",
            "avatar", "avatar_url", "avatarUrl",
            "thumbnail", "thumbnail_url", "thumbnailUrl",
            "profilePhotoUrl", "profile_photo_url",
            "profilePicUrl", "profile_pic_url",
        )

        /**
         * @since 1.1.0
         */
        private val imageUrlList: Sequence<String> = sequenceOf(
            "https://cdn.pixabay.com/photo/2023/10/14/09/20/mountains-8314422_1280.png",
            "https://cdn.pixabay.com/photo/2023/10/06/07/14/plant-8297610_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/09/04/19/10/butterfly-8233505_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/10/09/16/54/childrens-book-8304585_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/08/31/18/18/purple-coneflower-8225677_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/08/15/09/21/camera-8191564_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/02/19/25/woman-8361440_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/10/31/23/06/tiger-8356190_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/10/20/19/25/moon-8330104_1280.png",
            "https://cdn.pixabay.com/photo/2023/11/04/07/57/owl-8364426_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/10/10/07/59/lake-8305673_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/10/26/18/18/coneflower-8343278_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/02/11/32/woman-8360355_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/07/05/16/50/mountain-8108721_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/29/11/55/pine-hills-8419433_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/10/12/12/54/woman-8310743_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/07/12/55/wolf-8372315_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/26/10/26/piano-8413277_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/09/14/15/48/woman-8253239_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/12/07/10/59/giraffe-8435321_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/09/26/06/45/bride-8276620_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/08/30/04/16/man-8222531_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/07/31/17/06/couple-8161451_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/07/20/04/45/leva-8138344_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/04/21/15/42/portrait-7942151_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/04/28/07/16/man-7956041_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/24/18/52/cat-8410502_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/04/21/17/47/plum-blossoms-7942343_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/12/09/15/04/dog-8439530_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/11/30/07/51/bridge-8420945_1280.jpg",
            "https://cdn.pixabay.com/photo/2023/09/25/13/42/kingfisher-8275049_1280.png",
        )

        /**
         * @since 1.1.0
         */
        var iterator: Iterator<String> = imageUrlList.iterator()
    }


    /**
     * @return True when [property] is considered being image url.
     * @since 1.1.0
     */
    @Deprecated(message = "Refactor in 1.2.0, recognition and code generation in two steps meains more code and twice as time. Put recognition and generation in one step.")
    override fun recognize(property: ResolvedProperty, containingClassName: String): Boolean {

        if(!property.type.isString) {
            return false
        }

        val isClear = recognizableNames.contains(element = property.name)
        if (isClear) {
            return true
        }
        val isMaybe = recognizableNames.find { imgPropertyName ->
            property.name.contains(other = imgPropertyName, ignoreCase = true)
        } != null
        return isMaybe
    }


    /**
     * @return Code, and actual image url wrapped in "".
     * @since 1.1.0
     */
    @Deprecated(message = "Refactor in 1.2.0, recognition and code generation in two steps means more code and twice as time. Put recognition and generation in one step.")
    override fun generateCodeValueForProperty(
        property: ResolvedProperty
    ): String {
        return generateCodeBlockValueForProperty(property = property).toString()
    }


    private tailrec fun generateCodeBlockValueForProperty(
        property: ResolvedProperty
    ): CodeBlock {
        if (iterator.hasNext()) {
            val imageUrl = iterator.next()
            return CodeBlock.of("%S", imageUrl)
        }

        iterator = imageUrlList.iterator()
        return generateCodeBlockValueForProperty(property = property)
    }


    /**
     * @since 1.2.0
     */
    override fun tryRecognizeAndGenerateValue(
        property: ResolvedProperty,
        containingClassName: String
    ): String? {
        return tryRecognizeAndGenerateCodeBlock(
            property = property,
            containingClassName = containingClassName,
        )?.toString()
    }


    override fun tryRecognizeAndGenerateCodeBlock(
        property: ResolvedProperty,
        containingClassName: String,
    ): CodeBlock? {
        val isClear = recognizableNames.contains(element = property.name)
        if (isClear) {
            return generateCodeBlockValueForProperty(property = property)
        }
        val isMaybe = recognizableNames.find { imgPropertyName ->
            property.name.contains(other = imgPropertyName, ignoreCase = true)
        } != null

        return if (isMaybe) {
            generateCodeBlockValueForProperty(property = property)
        } else null
    }
}
