package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.LocalUdsExtendedColors
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    singleLine: Boolean = true,
    fillWidth: Boolean = true,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onImeAction: (() -> Unit)? = null
) {
    var sized = modifier
    sized = if (fillWidth) sized.fillMaxWidth() else sized
    sized = if (minHeight > 0.dp) sized.defaultMinSize(minHeight = minHeight) else sized

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = LocalUdsExtendedColors.current.textTertiary) },
        singleLine = singleLine,
        shape = RoundedCornerShape(Radii.Field),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        keyboardOptions = if (onImeAction != null) {
            KeyboardOptions(
                imeAction = ImeAction.Done
            )
        } else {
            KeyboardOptions.Default
        },
        keyboardActions = if (onImeAction != null) {
            KeyboardActions(
                onDone = { onImeAction() }
            )
        } else {
            KeyboardActions.Default
        },
        modifier = sized
    )
}

@PreviewLightDark
@Composable
private fun AppTextFieldPreview() {
    UdsTheme {
        var text by remember { mutableStateOf("") }
        Box(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            AppTextField(value = text, onValueChange = { text = it }, placeholder = "e.g. Pay electricity bill")
        }
    }
}
