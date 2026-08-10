package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownAnnotatorConfig
import com.mikepenz.markdown.model.markdownPadding
import com.seucaio.unideas.ds.components.inputs.BorderlessTextField
import com.seucaio.unideas.ds.theme.UdsTheme
import com.seucaio.unideas.feature.items.R
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownFormat
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownPreviewToggle
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.MarkdownToolbar
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.applyMarkdownFormat
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.markdownFormatContextMenuItems
import com.seucaio.unideas.feature.items.ui.components.fields.markdown.rememberMarkdownSyntaxHighlightTransformation

@Composable
internal fun TitleDescriptionFields(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    titleError: Boolean = false,
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
        TitleField(
            title = title,
            onTitleChanged = onTitleChanged,
            titleFocusRequester = titleFocusRequester,
            onImeAction = { descriptionFocusRequester.requestFocus() },
            titleError = titleError,
        )

        DescriptionField(
            isPreviewMode = isPreviewMode,
            onPreviewModeToggled = { isPreviewMode = !isPreviewMode },
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
private fun TitleField(
    title: String,
    onTitleChanged: (String) -> Unit,
    titleFocusRequester: FocusRequester,
    onImeAction: () -> Unit,
    titleError: Boolean,
    modifier: Modifier = Modifier,
) {
    BorderlessTextField(
        value = title,
        onValueChange = onTitleChanged,
        placeholder = stringResource(R.string.item_form_title_label),
        textStyle = MaterialTheme.typography.headlineLarge,
        modifier = modifier.focusRequester(titleFocusRequester),
        imeAction = ImeAction.Next,
        onImeAction = onImeAction,
        isError = titleError,
        supportingText = if (titleError) {
            { Text(stringResource(R.string.item_title_required)) }
        } else {
            null
        },
    )
}

@Composable
private fun DescriptionField(
    isPreviewMode: Boolean,
    onPreviewModeToggled: () -> Unit,
    descriptionField: TextFieldValue,
    onDescriptionFieldChanged: (TextFieldValue) -> Unit,
    descriptionFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val onFormatClick: (MarkdownFormat) -> Unit = { format ->
        onDescriptionFieldChanged(applyMarkdownFormat(descriptionField, format))
    }

    Column(modifier = modifier) {
        if (isPreviewMode) {
            SelectionContainer {
                Markdown(
                    content = descriptionField.text,
                    annotator = markdownAnnotator(config = markdownAnnotatorConfig(eolAsNewLine = true)),
                    // Library default is 2.dp between blocks (paragraph/list/checklist), far tighter
                    // than a blank line's line-height in the raw edit text — bumped so toggling
                    // edit<->preview doesn't visibly jump in height.
                    padding = markdownPadding(block = 0.dp),
                    modifier = Modifier.padding(16.dp),
                )
            }
        } else {
            BorderlessTextField(
                value = descriptionField,
                onValueChange = onDescriptionFieldChanged,
                placeholder = stringResource(R.string.item_form_description_label),
                singleLine = false,
                minHeight = 32.dp,
                modifier = Modifier
                    .focusRequester(descriptionFocusRequester)
                    .markdownFormatContextMenuItems(onFormatClick),
                visualTransformation = rememberMarkdownSyntaxHighlightTransformation(),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MarkdownPreviewToggle(isPreviewMode = isPreviewMode, onClick = onPreviewModeToggled)
            if (!isPreviewMode) {
                MarkdownToolbar(onFormatClick = onFormatClick)
            }
        }
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
