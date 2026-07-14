package com.seucaio.unideas.ds.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.theme.AccentContainer
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DividerColor
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.OnAccentContainer
import com.seucaio.unideas.ds.theme.TextTertiary

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun MetaChipsRow(label: String, chips: List<String>, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = AppType.Metadata, color = TextTertiary, fontSize = 13.sp)
            if (chips.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    chips.forEach { chip ->
                        Text(
                            chip,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnAccentContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(AccentContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@PreviewLightDark
@Composable
private fun MetaChipsRowPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            MetaChipsRow(label = "Tags", chips = listOf("urgent", "bills"))
        }
    }
}
