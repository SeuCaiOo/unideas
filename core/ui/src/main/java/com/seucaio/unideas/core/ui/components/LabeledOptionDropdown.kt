package com.seucaio.unideas.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.seucaio.unideas.core.ui.theme.UnideasTheme

/**
 * Generic "pick one of N options, always one selected" dropdown — an [OutlinedButton] showing
 * the current label that opens a [DropdownMenu] on click. Generic over [T] so any small closed
 * set (a domain enum/sealed type, mapped to `(value, label)` pairs by the caller) can reuse it —
 * `:core:ui` still never imports `:domain` directly. Different shape from [SectionDropdown]
 * (an [androidx.compose.material3.ExposedDropdownMenuBox] with a built-in "no selection" option)
 * on purpose: this one is for a field that always has a value, no "none" state.
 */
@Composable
fun <T> LabeledOptionDropdown(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(options.first { it.first == selected }.second)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun LabeledOptionDropdownPreview() {
    UnideasTheme {
        Surface {
            LabeledOptionDropdown(
                options = listOf("none" to "Nunca", "daily" to "Diário", "weekly" to "Semanal"),
                selected = "none",
                onSelect = {},
            )
        }
    }
}
