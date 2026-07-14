package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
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
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.Radii

@Composable
fun FilterDropdownPill(
    options: List<String>,
    selected: String,
    allOptionLabel: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Text(
            text = selected.ifEmpty { allOptionLabel },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.5.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(Radii.Chip))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(Radii.Chip))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 7.dp)
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(allOptionLabel) }, onClick = {
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
private fun FilterDropdownPillPreview() {
    DsTheme {
        var selected by remember { mutableStateOf("") }
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            FilterDropdownPill(
                options = listOf(
                    "Personal",
                    "Work"
                ),
                selected = selected,
                allOptionLabel = "All sections",
                onSelect = {
                    selected = it
                }
            )
        }
    }
}
