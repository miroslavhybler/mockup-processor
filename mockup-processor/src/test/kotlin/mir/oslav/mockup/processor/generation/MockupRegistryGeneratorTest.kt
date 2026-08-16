package mir.oslav.mockup.processor.generation

import mir.oslav.mockup.processor.data.MockupProviderHintData
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class MockupRegistryGeneratorTest {

    @Test
    fun generatedRegistryExposesGenericProviderHints() {
        val outputStream = ByteArrayOutputStream()

        MockupRegistryGenerator(outputStream = outputStream).generate(
            providers = emptyList(),
            providerHints = listOf(
                MockupProviderHintData(
                    rawClassName = "com.example.ListApiResponse",
                    targetTypeName = "com.example.ListOfUsersResponse",
                    providerClassName = "ListOfUsersResponseMockupProvider",
                    providerClassPackage = "com.example",
                    accessorName = "listOfUsersResponseMockupProvider",
                )
            ),
        )

        val generatedCode = outputStream.toString()
        assertTrue(generatedCode.contains("public object GeneratedMockupRegistry"))
        assertTrue(generatedCode.contains("@JvmStatic"))
        assertTrue(generatedCode.contains("public fun providerHints(): List<ProviderHint>"))
        assertTrue(generatedCode.contains("public data class ProviderHint"))
        assertTrue(generatedCode.contains("rawClassName = \"com.example.ListApiResponse\""))
        assertTrue(generatedCode.contains("targetTypeName = \"com.example.ListOfUsersResponse\""))
        assertTrue(generatedCode.contains("providerClassName = \"com.example.ListOfUsersResponseMockupProvider\""))
        assertTrue(generatedCode.contains("accessorName = \"listOfUsersResponseMockupProvider\""))
        assertTrue(generatedCode.contains("accessorImport = \"com.example.listOfUsersResponseMockupProvider\""))
    }
}
