@file:Suppress("SpellCheckingInspection")

package mir.oslav.mockup.processor.recognition

import com.squareup.kotlinpoet.CodeBlock
import mir.oslav.mockup.processor.data.ResolvedProperty
import mir.oslav.mockup.processor.generation.isString


/**
 * Recognizer for userNames including first name, last name and nickname. Tryes to recognize and
 * generate userName code value for the properties. UserName can be a string only.
 * @since 1.1.3
 * @author Miroslav Hýbler <br>
 * created on 01.01.2024
 */
class UsernameRecognizer constructor() : BaseRecognizer() {


    companion object {

        /**
         * Properties names that are 100% they are userName no matter in which class they are.
         * @since 1.1.3
         */
        private val primaryRecognizablePropertiesNames: List<String> = listOf(
            "username", "userName", "user_name",
            "nickname", "nickName", "nick_name"
        )


        /**
         * Helps with additianal recognition in "person" context classes. Used in combination with
         * [recognizableClassNames], e.g. recognizer things that propetry "name" in class "User" is userName.
         * @since 1.1.3
         */
        private val secondaryRecognizablePropertiesNames: List<String> = listOf(
            "name"
        )


        /**
         * Properties names that will be used to recognize first name
         * @since 1.1.3
         */
        private val recognizableFirstNames: List<String> = listOf(
            "firstname", "firstName", "first_name",
            "givenname", "givenName", "given_name",
            "forename", "foreName", "fore_name"
        )


        /**
         * Properties names that will be used to recognize last name
         * @since 1.1.3
         */
        private val recognizableLastNames: List<String> = listOf(
            "lastname", "lastName", "last_name",
            "surname", "surName", "sur_name",
            "secondname", "secondName", "second_name",
            "birthname", "birthName", "birth_name"
        )


        /**
         * Helps recognizer to recognize if class is probably representing a "Person". This is used in
         * combination with [secondaryRecognizablePropertiesNames] so e.g. recognizer things that
         * propetry "name" in class "User" is userName.
         * @since 1.1.3
         */
        private val recognizableClassNames: List<String> = listOf(
            "User", "Profile", "UserProfile", "Person", "Employee", "Client"
        )


        /**
         * List of first names used for generation
         * @since 1.1.3
         */
        private val firstNames: Sequence<String> = sequenceOf(
            "Wade", "Dave", "Daisy", "Seth", "Ivan", "Beverly", "Vera", "Angela",
            "Riley", "Gilbert", "Jorge", "Dan", "Brian",
            "Roberto", "Ramon", "Miles", "Liam", "Nathaniel", "Ethan", "Lewis", "Milton",
            "Claude", "Joshua", "Glen", "Harvey", "Blake", "Antonio", "Connor", "Julian",
            "Aidan", "Harold", "Conner", "Peter", "Hunter", "Eli", "Alberto", "Carlos",
            "Shane", "Aaron", "Marlin", "Paul", "Ricardo", "Hector", "Alexis", "Adrian",
            "Kingston", "Douglas", "Gerald", "Joey", "Johnny", "Charlie", "Scott", "Martin",
            "Tristin", "Troy", "Tommy", "Rick", "Victor", "Jessie", "Neil", "Ted", "Nick",
            "Wiley", "Morris", "Clark", "Stuart", "Orlando", "Keith", "Marion", "Marshall",
            "Noel", "Everett", "Romeo", "Sebastian", "Stefan", "Robin", "Clarence", "Sandy",
            "Ernest", "Samuel", "Benjamin", "Luka", "Fred", "Albert", "Greyson", "Terry",
            "Cedric", "Joe", "Paul", "George", "Bruce", "Christopher", "Mark", "Ron", "Craig",
            "Philip", "Jimmy", "Arthur", "Jaime", "Perry", "Harold", "Jerry", "Shawn", "Walter"
        )


        /**
         * List of last names used for generation
         * @since 1.1.3
         */
        private val lastNames: Sequence<String> = sequenceOf(
            "Williams", "Harris", "Thomas", "Robinson", "Walker", "Scott", "Nelson", "Mitchell",
            "Morgan", "Cooper", "Howard", "Davis", "Miller", "Martin", "Smith", "Anderson",
            "White",
            "Perry", "Clark", "Richards", "Wheeler", "Warburton", "Stanley", "Holland", "Terry",
            "Shelton", "Miles", "Lucas", "Fletcher", "Parks", "Norris", "Guzman", "Daniel",
            "Newton",
            "Potter", "Francis", "Erickson", "Norman", "Moody", "Lindsey", "Gross", "Sherman",
            "Simon", "Jones", "Brown", "Garcia", "Rodriguez", "Lee", "Young", "Hall", "Allen",
            "Lopez", "Green", "Gonzalez", "Baker", "Adams", "Perez", "Campbell", "Shaw", "Gordon",
            "Burns", "Warren",
            "Long", "McDonald", "Gibson", "Ellis", "Fisher", "Reynolds", "Jordan", "Hamilton",
            "Ford",
            "Graham", "Griffin", "Russell", "Foster", "Butler", "Simmons", "Flores", "Bennett",
            "Sanders", "Hughes", "Bryant", "Patterson", "Matthews", "Jenkins", "Watkins", "Ward",
            "Murphy", "Bailey", "Bell", "Cox", "Martinez", "Evans", "Rivera", "Peterson", "Gomez",
            "Murray", "Tucker", "Hicks", "Crawford"
        )


        /**
         * Iterator for [firstNames]
         * @since 1.1.3
         */
        private var firstNamesIterator: Iterator<String> = firstNames.iterator()


        /**
         * Iterator for [lastNames]
         * @since 1.1.3
         */
        private var lastNamesIterator: Iterator<String> = lastNames.iterator()
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
        if (!property.type.isString) {
            //Only string can be username
            return null
        }


        val isPropertyClear = primaryRecognizablePropertiesNames.contains(element = property.name)
        val isUserClassContext = recognizableClassNames.contains(element = containingClassName)
        val isFirstName = recognizableFirstNames.contains(element = property.name)
        val isLastName = recognizableLastNames.contains(element = property.name)

        val isRecognized = if (isUserClassContext) {
            val isClear = secondaryRecognizablePropertiesNames.contains(element = property.name)
            isPropertyClear || isClear || isFirstName || isLastName
        } else isPropertyClear || isFirstName || isLastName

        if (isRecognized) {
            if (!firstNamesIterator.hasNext()) {
                firstNamesIterator = firstNames.iterator()
                return generateCodeBlockValueForProperty(property = property)
            }
            if (!lastNamesIterator.hasNext()) {
                lastNamesIterator = lastNames.iterator()
                return generateCodeBlockValueForProperty(property = property)
            }

            val username = when {
                isFirstName -> firstNamesIterator.next()
                isLastName -> lastNamesIterator.next()
                else -> "${firstNamesIterator.next()} ${lastNamesIterator.next()}"
            }
            return CodeBlock.of("%S", username)
        }

        return null
    }


    /**
     * @since 1.1.3
     */
    @Deprecated(message = "Refactor in 1.2.0, recognition and code generation in two steps meains more code and twice as time. Put recognition and generation in one step.")
    override fun recognize(property: ResolvedProperty, containingClassName: String): Boolean {
        if (!property.type.isString) {
            //Only string can be username
            return false
        }


        val isPropertyClear = primaryRecognizablePropertiesNames.contains(element = property.name)
        val isUserClassContext = recognizableClassNames.contains(element = containingClassName)
        val isFirstName = recognizableFirstNames.contains(element = property.name)
        val isLastName = recognizableLastNames.contains(element = property.name)

        return if (isUserClassContext) {
            val isClear = secondaryRecognizablePropertiesNames.contains(element = property.name)
            isPropertyClear || isClear || isFirstName || isLastName
        } else isPropertyClear || isFirstName || isLastName
    }


    /**
     * @since 1.1.3
     */
    @Deprecated(message = "Refactor in 1.2.0, recognition and code generation in two steps means more code and twice as time. Put recognition and generation in one step.")
    override fun generateCodeValueForProperty(property: ResolvedProperty): String {
        return generateCodeBlockValueForProperty(property = property).toString()
    }


    private fun generateCodeBlockValueForProperty(property: ResolvedProperty): CodeBlock {
        if (!firstNamesIterator.hasNext()) {
            firstNamesIterator = firstNames.iterator()
            return generateCodeBlockValueForProperty(property = property)
        }

        if (!lastNamesIterator.hasNext()) {
            lastNamesIterator = lastNames.iterator()
            return generateCodeBlockValueForProperty(property = property)
        }

        val isFirstName = recognizableFirstNames.contains(element = property.name)
        val isLastName = recognizableLastNames.contains(element = property.name)


        val username = when {
            isFirstName -> firstNamesIterator.next()
            isLastName -> lastNamesIterator.next()
            else -> "${firstNamesIterator.next()} ${lastNamesIterator.next()}"
        }
        return CodeBlock.of("%S", username)
    }
}
