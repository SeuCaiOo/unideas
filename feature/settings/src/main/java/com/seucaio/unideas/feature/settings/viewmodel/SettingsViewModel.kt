package com.seucaio.unideas.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/** No use cases yet — the shell only navigates to Sections/Tags; Backup status arrives in E2. */
class SettingsViewModel : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState.Success(BackupStatus.DISCONNECTED)).asStateFlow()

    private val _action = Channel<SettingsUiAction>(Channel.BUFFERED)
    val action: Flow<SettingsUiAction> = _action.receiveAsFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnOrganizeSectionsClicked -> send(SettingsUiAction.NavigateToSections)
            SettingsEvent.OnOrganizeTagsClicked -> send(SettingsUiAction.NavigateToTags)
        }
    }

    private fun send(action: SettingsUiAction) = viewModelScope.launch {
        _action.send(action)
    }
}
