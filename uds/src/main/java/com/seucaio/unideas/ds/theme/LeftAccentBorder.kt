package com.seucaio.unideas.ds.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

fun Modifier.leftAccentBorder(width: Dp, color: Color): Modifier = drawBehind {
    drawRect(color, size = Size(width.toPx(), size.height))
}
