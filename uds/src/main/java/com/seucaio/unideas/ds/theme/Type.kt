package com.seucaio.unideas.ds.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

// Roboto is Android's default system sans-serif; no bundled font files needed.
private val Roboto = FontFamily.SansSerif

/** Hand-picked text styles mirroring the design tokens exactly. */
object AppType {
    val AppTitle = TextStyle(
        fontFamily = Roboto,
        fontSize = 23.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.2.sp
    )
    val ScreenTitle = TextStyle(fontFamily = Roboto, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    val ItemTitleDetail = TextStyle(
        fontFamily = Roboto,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp
    )
    val ListItemTitle = TextStyle(fontFamily = Roboto, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    val Body = TextStyle(fontFamily = Roboto, fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp)
    val FieldLabel = TextStyle(
        fontFamily = Roboto,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.9.sp
    )
    val Metadata = TextStyle(fontFamily = Roboto, fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val MetadataMedium = TextStyle(fontFamily = Roboto, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
    val DueBadge = TextStyle(fontFamily = Roboto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    val TabLabel = TextStyle(
        fontFamily = Roboto,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    )
    val ButtonLabel = TextStyle(fontFamily = Roboto, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    val ButtonLabelLarge = TextStyle(fontFamily = Roboto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    val ChipLabel = TextStyle(fontFamily = Roboto, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
    val TypeBadge = TextStyle(
        fontFamily = Roboto,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
    )
}

val AppTypography = Typography(
    bodyLarge = AppType.Body,
    titleLarge = AppType.ScreenTitle,
    labelLarge = AppType.ButtonLabel
)
