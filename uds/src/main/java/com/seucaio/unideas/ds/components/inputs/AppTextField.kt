package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.seucaio.unideas.ds.theme.Accent
import com.seucaio.unideas.ds.theme.Background
import com.seucaio.unideas.ds.theme.DsTheme
import com.seucaio.unideas.ds.theme.Outline
import com.seucaio.unideas.ds.theme.Radii
import com.seucaio.unideas.ds.theme.Surface1
import com.seucaio.unideas.ds.theme.TextPrimary
import com.seucaio.unideas.ds.theme.TextTertiary

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    singleLine: Boolean = true,
    fillWidth: Boolean = true,
    borderColor: Color = Outline,
    onImeAction: (() -> Unit)? = null
) {
    var sized = modifier
    sized = if (fillWidth) sized.fillMaxWidth() else sized
    sized = if (minHeight > 0.dp) sized.defaultMinSize(minHeight = minHeight) else sized

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextTertiary) },
        singleLine = singleLine,
        shape = RoundedCornerShape(Radii.Field),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface1,
            unfocusedContainerColor = Surface1,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Accent
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
    DsTheme {
        var text by remember { mutableStateOf("") }
        Box(Modifier.background(Background).padding(16.dp)) {
            AppTextField(value = text, onValueChange = { text = it }, placeholder = "e.g. Pay electricity bill")
        }
    }
}
