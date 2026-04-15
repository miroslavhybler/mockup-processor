package com.example.app.ui.custom

import com.mockup.core.CustomMockupProvider
import com.mockup.core.Mockup
import kotlin.reflect.KClass

object PromoCardCustomProvider : CustomMockupProvider<PromoCard> {
    override val clazz: KClass<PromoCard> = PromoCard::class
    override val values: List<PromoCard> = Mockup.fromJsonList(
        json = """
[
  {
    "id": 101,
    "title": "Starter Pack",
    "subtitle": "Everything you need to begin",
    "price": "${'$'}9 / mo",
    "isFeatured": true
  },
  {
    "id": 202,
    "title": "Team Bundle",
    "subtitle": "Shared tools for small teams",
    "price": "${'$'}29 / mo",
    "isFeatured": false
  },
  {
    "id": 303,
    "title": "Enterprise",
    "subtitle": "Custom support and security",
    "price": "Contact us",
    "isFeatured": true
  }
]
        """.trimIndent()
    )
}
