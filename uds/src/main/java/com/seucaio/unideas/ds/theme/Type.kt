package com.seucaio.unideas.ds.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Roboto is Android's default system sans-serif; no bundled font files needed.
private val Roboto = FontFamily.SansSerif

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = Roboto,
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 30.sp
    ),
    headlineMedium = TextStyle(fontFamily = Roboto, fontSize = 15.sp, fontWeight = FontWeight.Medium),
    headlineSmall = TextStyle(
        fontFamily = Roboto,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center
    ),
    titleLarge = TextStyle(fontFamily = Roboto, fontSize = 19.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = Roboto, fontSize = 12.5.sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontFamily = Roboto, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontSize = 15.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(fontFamily = Roboto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp
    ),
    labelLarge = TextStyle(fontFamily = Roboto, fontSize = 14.sp, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(
        fontFamily = Roboto,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.9.sp
    )
)

@PreviewLightDark
@Composable
private fun AppTypographyPreview() {
    UdsTheme {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TypeSample("headlineLarge", MaterialTheme.typography.headlineLarge, "Renew car insurance")
            TypeSample("headlineMedium", MaterialTheme.typography.headlineMedium, "Pay electricity bill")
            TypeSample("headlineSmall", MaterialTheme.typography.headlineSmall, "Tasks")
            TypeSample("titleLarge", MaterialTheme.typography.titleLarge, "Item detail")
            TypeSample("titleMedium", MaterialTheme.typography.titleMedium, "urgent")
            TypeSample("titleSmall", MaterialTheme.typography.titleSmall, "Home \u00b7 6 items")
            TypeSample("bodyLarge", MaterialTheme.typography.bodyLarge, "Don't forget to bring the documents.")
            TypeSample("bodyMedium", MaterialTheme.typography.bodyMedium, "3 days overdue")
            TypeSample("bodySmall", MaterialTheme.typography.bodySmall, "TASK")
            TypeSample("labelLarge", MaterialTheme.typography.labelLarge, "SAVE")
            TypeSample("labelMedium", MaterialTheme.typography.labelMedium, "TITLE")
        }
    }
}

@Composable
private fun TypeSample(slotName: String, style: TextStyle, sampleText: String) {
    Column {
        Text(slotName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(sampleText, style = style, color = MaterialTheme.colorScheme.onSurface)
    }
}
