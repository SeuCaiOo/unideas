package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.seucaio.unideas.ds.components.inputs.BorderlessTextField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownFormat
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownPreviewToggle
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownToolbar
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.applyMarkdownFormat
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.markdownFormatContextMenuItems

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
    // Existing items open read-only (preview); a brand-new item has nothing to preview yet.
    var isPreviewMode by remember { mutableStateOf(isEditing) }

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

    Column(modifier = modifier.animateContentSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BorderlessTextField(
                value = title,
                onValueChange = onTitleChanged,
                placeholder = stringResource(R.string.item_form_title_label),
                textStyle = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(titleFocusRequester),
                imeAction = ImeAction.Next,
                onImeAction = { descriptionFocusRequester.requestFocus() },
            )
            MarkdownPreviewToggle(
                isPreviewMode = isPreviewMode,
                onClick = { isPreviewMode = !isPreviewMode },
            )
        }

        DescriptionField(
            isPreviewMode = isPreviewMode,
            descriptionField = descriptionField,
            onDescriptionFieldChanged = {
                descriptionField = it
                onDescriptionChanged(it.text)
            },
            descriptionFocusRequester = descriptionFocusRequester,
        )
    }
}

@Composable
private fun DescriptionField(
    isPreviewMode: Boolean,
    descriptionField: TextFieldValue,
    onDescriptionFieldChanged: (TextFieldValue) -> Unit,
    descriptionFocusRequester: FocusRequester,
) {
    if (isPreviewMode) {
        // Same size as the edit field (titleLarge) but Normal weight, so **bold**/*italic* markers
        // remain visually distinct from plain text instead of blending into an already-heavy base.
        val previewTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)
        Markdown(
            content = descriptionField.text,
            typography = markdownTypography(
                text = previewTextStyle,
                paragraph = previewTextStyle,
                ordered = previewTextStyle,
                bullet = previewTextStyle,
                list = previewTextStyle,
            ),
            annotator = markdownAnnotator(config = markdownAnnotatorConfig(eolAsNewLine = true)),
            modifier = Modifier.padding(vertical = 16.dp),
        )
    } else {
        val onFormatClick: (MarkdownFormat) -> Unit = { format ->
            onDescriptionFieldChanged(applyMarkdownFormat(descriptionField, format))
        }

        BorderlessTextField(
            value = descriptionField,
            onValueChange = onDescriptionFieldChanged,
            placeholder = stringResource(R.string.item_form_description_label),
            singleLine = false,
            minHeight = 32.dp,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .focusRequester(descriptionFocusRequester)
                .markdownFormatContextMenuItems(onFormatClick),
            textStyle = MaterialTheme.typography.titleLarge,
        )

        MarkdownToolbar(onFormatClick = onFormatClick)
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
