package com.example.app.data

import com.mockup.core.Mockup
import com.mockup.exampledata.Article
import com.mockup.exampledata.Publisher
import com.mockup.exampledata.Reader

/**
 * Supplies screen data only to keep the example application interactive as a library showcase.
 *
 * This is intentionally not the recommended integration for production apps. Real screens should
 * receive state from their normal data sources (REST APIs, databases, repositories, and so on) and
 * use `Mockup` only inside Compose previews. The debug variant reads generated mockup values;
 * release and benchmark variants return empty lists so neither generated data nor `mockup-core`
 * becomes a production runtime dependency.
 */
internal object ShowcaseDataSource {
    val articles: List<Article> by lazy { Mockup.getList() }

    val publishers: List<Publisher> by lazy { Mockup.getList() }

    val followers: List<Reader> by lazy {
        Mockup.fromJsonList(
            json = """
                [
                  {
                    "userName": { "surname": "John", "birthname": "Doe" },
                    "item": { "id": 1, "name": "Kotlin Weekly" }
                  },
                  {
                    "userName": { "surname": "Jane", "birthname": "Doe" },
                    "item": { "id": 2, "name": "Compose Digest" }
                  }
                ]
            """.trimIndent()
        )
    }
}
