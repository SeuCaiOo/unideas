package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.AppType
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.TextPrimary
import com.seucaio.unideas.ds.theme.TextTertiary

@Composable
fun FormField(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier) {
        Text(
            label.uppercase(),
            style = AppType.FieldLabel,
            color = TextTertiary,
            modifier = Modifier.padding(bottom = 7.dp)
        )
        content()
    }
}

@PreviewLightDark
@Composable
private fun FormFieldPreview() {
    DsTheme {
        Box(Modifier.background(Background).padding(16.dp)) {
            FormField(label = "Title") { Text("any content goes here", color = TextPrimary) }
        }
    }
}
