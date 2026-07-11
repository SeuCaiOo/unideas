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

    private val _uiAction = Channel<SettingsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<SettingsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnOrganizeSectionsClicked -> sendUiAction(SettingsUiAction.NavigateToSections)
            SettingsEvent.OnOrganizeTagsClicked -> sendUiAction(SettingsUiAction.NavigateToTags)
        }
    }

    private fun sendUiAction(action: SettingsUiAction) = viewModelScope.launch {
        _uiAction.send(action)
    }
}
