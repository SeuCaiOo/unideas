package com.seucaio.unideas.feature.items.ui.screens.config.sectionstags.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SectionsTagsViewModel(
    private val sectionUseCase: SectionUseCase,
    private val tagUseCase: TagUseCase,
) : ViewModel() {

    val uiState: StateFlow<SectionsTagsUiState> = combine(
        sectionUseCase.getAll().catch { emit(emptyList()) },
        tagUseCase.getAll().catch { emit(emptyList()) },
    ) { sections, tags -> SectionsTagsUiState(sections = sections, tags = tags) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SectionsTagsUiState())

    private val _uiAction = Channel<SectionsTagsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<SectionsTagsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: SectionsTagsEvent) {
        when (event) {
            is SectionsTagsEvent.OnSectionCreateRequested -> handleCreateSection(event.name)
            is SectionsTagsEvent.OnTagCreateRequested -> handleCreateTag(event.name)
        }
    }

    private fun handleCreateSection(name: String) = viewModelScope.launch {
        sectionUseCase.add(name).onFailure { handleFailure(it, R.string.item_config_section_name_required) }
    }

    private fun handleCreateTag(name: String) = viewModelScope.launch {
        tagUseCase.add(name).onFailure { handleFailure(it, R.string.item_config_tag_name_required) }
    }

    private suspend fun handleFailure(error: Throwable, nameRequiredRes: Int) {
        if (error is IllegalArgumentException) {
            sendUiAction(SectionsTagsUiAction.ShowSnackbar(nameRequiredRes))
        } else {
            sendUiAction(SectionsTagsUiAction.ShowError(error.message.orEmpty()))
        }
    }

    private suspend fun sendUiAction(action: SectionsTagsUiAction) = _uiAction.send(action)
}
