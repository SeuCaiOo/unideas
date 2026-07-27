package com.seucaio.unideas.feature.items.ui.components.fields

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class TitleDescriptionPreviewData(
    val title: String,
    val description: String,
    val isEditing: Boolean = true,
)

class TitleDescriptionFieldsPreviewProvider : PreviewParameterProvider<TitleDescriptionPreviewData> {

    override val values: Sequence<TitleDescriptionPreviewData> = sequenceOf(
        // New item, nothing typed yet.
        TitleDescriptionPreviewData(title = "", description = "", isEditing = false),
        // Plain text, no Markdown at all.
        TitleDescriptionPreviewData(
            title = "Comprar mantimentos",
            description = "leite, ovos, pão",
        ),
        // Every supported Markdown element at once.
        TitleDescriptionPreviewData(
            title = "Preparar apresentação",
            description = "**Urgente** — revisar até *sexta*\n\n" +
                "- slides\n" +
                "- ~~introdução~~ (pronta)\n" +
                "- [ ] conclusão\n" +
                "- [x] dados",
        ),
        // Long, multiline description to check scrolling/wrapping.
        TitleDescriptionPreviewData(
            title = "Anotação longa",
            description = (1..8).joinToString("\n") {
                "Linha $it de uma descrição bem mais longa, pra testar quebra de linha e scroll."
            },
        ),
    )
}
