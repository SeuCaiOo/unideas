package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.Outline
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.Surface1
import com.seucaio.unideas.ds.theme.TextPrimary
import com.seucaio.unideas.ds.theme.TextTertiary

@Composable
fun DateFieldButton(
    valueLabel: String?,
    onClick: () -> Unit,
    onClear: () -> Unit,
    clearContentDescription: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Field))
            .background(Surface1)
            .border(1.dp, Outline, RoundedCornerShape(Radii.Field))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            valueLabel ?: "",
            color = if (valueLabel != null) TextPrimary else TextTertiary,
            fontSize = 15.sp
        )
        if (valueLabel != null) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = clearContentDescription,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp).clickable(onClick = onClear)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DateFieldButtonPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            DateFieldButton(valueLabel = "Jul 14, 2026", onClick = {}, onClear = {}, clearContentDescription = "Clear")
        }
    }
}
