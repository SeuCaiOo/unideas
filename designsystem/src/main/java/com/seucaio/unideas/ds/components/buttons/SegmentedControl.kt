package com.seucaio.unideas.ds.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.theme.AccentContainer
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.OnAccentContainer
import com.seucaio.unideas.ds.theme.Outline
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.TextSecondary

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Segmented))
            .border(1.dp, Outline, RoundedCornerShape(Radii.Segmented))
    ) {
        options.forEachIndexed { index, label ->
            SegmentButton(
                label = label,
                selected = index == selectedIndex,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun SegmentButton(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(if (selected) AccentContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) OnAccentContainer else TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@PreviewLightDark
@Composable
private fun SegmentedControlPreview() {
    DsTheme {
        var selected by remember { mutableIntStateOf(0) }
        Box(Modifier.background(Background).padding(16.dp)) {
            SegmentedControl(options = listOf("Task", "Note"), selectedIndex = selected, onSelect = { selected = it })
        }
    }
}
