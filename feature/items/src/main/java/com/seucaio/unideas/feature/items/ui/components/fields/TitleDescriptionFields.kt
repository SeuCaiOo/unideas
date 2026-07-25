package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.seucaio.unideas.ds.components.inputs.BorderlessTextField
import com.seucaio.unideas.feature.items.R

@Composable
internal fun TitleDescriptionFields(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    isEditing: Boolean,
) {
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (!isEditing) {
            titleFocusRequester.requestFocus()
        }
    }

    BorderlessTextField(
        value = title,
        onValueChange = onTitleChanged,
        placeholder = stringResource(R.string.item_form_title_label),
        textStyle = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.Companion.focusRequester(titleFocusRequester),
        imeAction = ImeAction.Companion.Next,
        onImeAction = { descriptionFocusRequester.requestFocus() },
    )

    BorderlessTextField(
        value = description,
        onValueChange = onDescriptionChanged,
        placeholder = stringResource(R.string.item_form_description_label),
        singleLine = false,
        minHeight = 32.dp,
        modifier = Modifier.Companion
            .padding(vertical = 16.dp)
            .focusRequester(descriptionFocusRequester),
        textStyle = MaterialTheme.typography.titleLarge,
    )
}
