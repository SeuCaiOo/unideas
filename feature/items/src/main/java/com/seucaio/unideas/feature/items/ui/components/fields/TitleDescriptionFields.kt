package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.seucaio.unideas.ds.components.inputs.BorderlessTextField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownToolbar
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.applyMarkdownFormat

@Composable
internal fun TitleDescriptionFields(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
) {
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    var descriptionField by remember { mutableStateOf(TextFieldValue(description)) }
    var isPreviewMode by remember { mutableStateOf(false) }

    LaunchedEffect(description) {
        if (description != descriptionField.text) {
            descriptionField = TextFieldValue(description)
        }
    }

    LaunchedEffect(Unit) {
        if (!isEditing) {
            titleFocusRequester.requestFocus()
        }
    }

    Column(modifier = modifier) {
        BorderlessTextField(
            value = title,
            onValueChange = onTitleChanged,
            placeholder = stringResource(R.string.item_form_title_label),
            textStyle = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.focusRequester(titleFocusRequester),
            imeAction = ImeAction.Next,
            onImeAction = { descriptionFocusRequester.requestFocus() },
        )

        if (isPreviewMode) {
            Markdown(
                content = descriptionField.text,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        } else {
            BorderlessTextField(
                value = descriptionField,
                onValueChange = {
                    descriptionField = it
                    onDescriptionChanged(it.text)
                },
                placeholder = stringResource(R.string.item_form_description_label),
                singleLine = false,
                minHeight = 32.dp,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .focusRequester(descriptionFocusRequester),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        }

        MarkdownToolbar(
            isPreviewMode = isPreviewMode,
            onFormatClick = { format ->
                descriptionField = applyMarkdownFormat(descriptionField, format)
                onDescriptionChanged(descriptionField.text)
            },
            onPreviewToggle = { isPreviewMode = !isPreviewMode },
        )
    }
}

@PreviewLightDark
@Composable
private fun TitleDescriptionFieldsPreview(
    @PreviewParameter(TitleDescriptionFieldsPreviewProvider::class) previewData: TitleDescriptionPreviewData,
) {
    UdsTheme {
        Surface {
            var title by remember { mutableStateOf(previewData.title) }
            var description by remember { mutableStateOf(previewData.description) }
            TitleDescriptionFields(
                title = title,
                description = description,
                onTitleChanged = { title = it },
                onDescriptionChanged = { description = it },
                isEditing = previewData.isEditing,
            )
        }
    }
}
