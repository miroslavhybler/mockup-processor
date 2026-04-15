@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.app.ui.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mockup.annotations.Mockup
import kotlinx.serialization.Serializable

@Mockup(count = 2)
@Serializable
data class PromoCard(
    val id: Int,
    val title: String,
    val subtitle: String,
    val price: String,
    val isFeatured: Boolean,
) {
}


@Composable
private fun PromoCardList(
    cards: List<PromoCard>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp),
    ) {
        cards.forEach { card ->
            val colors = if (card.isFeatured) {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                CardDefaults.cardColors()
            }

            Card(
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(space = 4.dp),
                    modifier = Modifier.padding(all = 16.dp),
                ) {
                    Text(text = card.title, style = MaterialTheme.typography.titleMedium)
                    Text(text = card.subtitle, style = MaterialTheme.typography.bodyMedium)
                    Text(text = card.price, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
private fun PromoCardListPreview() {
    PromoCardList(
        cards = com.mockup.core.Mockup.getList<PromoCard>().take(4)
    )
}
