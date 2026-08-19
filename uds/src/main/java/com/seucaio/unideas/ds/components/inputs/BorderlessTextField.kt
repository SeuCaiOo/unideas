package com.seucaio.unideas.ds.components.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.theme.UdsTheme

@Composable
fun BorderlessTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    singleLine: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    var sized = modifier.fillMaxWidth()
    if (minHeight > 0.dp) sized = sized.defaultMinSize(minHeight = minHeight)

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = textStyle) },
        singleLine = singleLine,
        textStyle = textStyle,
        isError = isError,
        supportingText = supportingText,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
        ),
        keyboardOptions = if (onImeAction != null) {
            KeyboardOptions(imeAction = imeAction)
        } else {
            KeyboardOptions.Default
        },
        keyboardActions = if (onImeAction != null) {
            KeyboardActions(
                onDone = { if (imeAction == ImeAction.Done) onImeAction() },
                onNext = { if (imeAction == ImeAction.Next) onImeAction() },
            )
        } else {
            KeyboardActions.Default
        },
        modifier = sized,
    )
}

@Composable
fun BorderlessTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    singleLine: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var sized = modifier.fillMaxWidth()
    if (minHeight > 0.dp) sized = sized.defaultMinSize(minHeight = minHeight)

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = textStyle) },
        singleLine = singleLine,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = if (onImeAction != null) {
            KeyboardOptions(imeAction = imeAction)
        } else {
            KeyboardOptions.Default
        },
        keyboardActions = if (onImeAction != null) {
            KeyboardActions(
                onDone = { if (imeAction == ImeAction.Done) onImeAction() },
                onNext = { if (imeAction == ImeAction.Next) onImeAction() },
            )
        } else {
            KeyboardActions.Default
        },
        modifier = sized,
    )
}

private enum class BorderlessTextFieldPreviewScenario {
    Empty, Filled, MultiLine, Error
}

private class BorderlessTextFieldPreviewProvider : PreviewParameterProvider<BorderlessTextFieldPreviewScenario> {
    override val values = BorderlessTextFieldPreviewScenario.entries.asSequence()
}

@PreviewLightDark
@Composable
private fun BorderlessTextFieldPreview(
    @PreviewParameter(BorderlessTextFieldPreviewProvider::class) scenario: BorderlessTextFieldPreviewScenario,
) {
    UdsTheme {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            when (scenario) {
                BorderlessTextFieldPreviewScenario.Empty -> {
                    var text by remember { mutableStateOf("") }
                    BorderlessTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = "Untitled",
                        textStyle = MaterialTheme.typography.headlineLarge,
                    )
                }
                BorderlessTextFieldPreviewScenario.Filled -> {
                    var text by remember { mutableStateOf("Pay electricity bill") }
                    BorderlessTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = "Untitled",
                        textStyle = MaterialTheme.typography.headlineLarge,
                    )
                }
                BorderlessTextFieldPreviewScenario.MultiLine -> {
                    var text by remember { mutableStateOf("Renew the annual streaming subscription before it lapses") }
                    BorderlessTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = "Description",
                        minHeight = 80.dp,
                        singleLine = false,
                        textStyle = MaterialTheme.typography.bodyLarge,
                    )
                }
                BorderlessTextFieldPreviewScenario.Error -> {
                    var text by remember { mutableStateOf("") }
                    BorderlessTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = "Untitled",
                        textStyle = MaterialTheme.typography.headlineLarge,
                        isError = true,
                        supportingText = { Text("Title is required") },
                    )
                }
            }
        }
    }
}
