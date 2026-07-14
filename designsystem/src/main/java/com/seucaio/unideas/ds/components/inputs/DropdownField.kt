package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun DropdownField(
    options: List<String>,
    selected: String,
    emptyOptionLabel: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Radii.Field))
                .background(Surface1)
                .border(1.dp, Outline, RoundedCornerShape(Radii.Field))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            Text(selected.ifEmpty { emptyOptionLabel }, color = TextPrimary, fontSize = 15.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(emptyOptionLabel) }, onClick = {
                onSelect("")
                expanded = false
            })
            options.forEach { name ->
                DropdownMenuItem(text = { Text(name) }, onClick = {
                    onSelect(name)
                    expanded = false
                })
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun DropdownFieldPreview() {
    DsTheme {
        var selected by remember { mutableStateOf("") }
        Box(Modifier.background(Background).padding(16.dp)) {
            DropdownField(
                options = listOf(
                    "Personal",
                    "Work"
                ),
                selected = selected,
                emptyOptionLabel = "No section",
                onSelect = {
                    selected = it
                }
            )
        }
    }
}
