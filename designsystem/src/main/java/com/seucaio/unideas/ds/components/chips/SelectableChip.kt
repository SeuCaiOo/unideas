package com.seucaio.unideas.ds.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AccentContainer
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.OnAccentContainer
import com.seucaio.unideas.ds.theme.Outline
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.TextSecondary

@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(Radii.Chip)
    val background = if (selected) AccentContainer else Color.Transparent
    val border = if (selected) AccentContainer else Outline
    val content = if (selected) OnAccentContainer else TextSecondary
    Text(
        text = label,
        style = AppType.ChipLabel,
        color = content,
        modifier = modifier
            .clip(shape)
            .background(background, shape)
            .border(1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@PreviewLightDark
@Composable
private fun SelectableChipPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(label = "urgent", selected = true, onClick = {})
                SelectableChip(label = "market", selected = false, onClick = {})
            }
        }
    }
}
