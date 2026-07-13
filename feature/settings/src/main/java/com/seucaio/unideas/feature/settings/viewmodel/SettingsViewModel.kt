package com.seucaio.unideas.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.domain.usecase.settings.ClearDatabaseUseCase
import com.seucaio.unideas.domain.usecase.settings.SeedDatabaseUseCase
import com.seucaio.unideas.feature.settings.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * No use cases yet for the real screen state — the shell only navigates to Sections/Tags; Backup
 * connection state is collected directly from `BackupViewModel` by the Screen, not through here.
 * [SeedDatabaseUseCase]/[ClearDatabaseUseCase] are debug-only tooling (#19), triggered from
 * buttons the Screen only renders when `BuildConfig.DEBUG`.
 */
class SettingsViewModel(
    private val seedDatabase: SeedDatabaseUseCase,
    private val clearDatabase: ClearDatabaseUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState.Success).asStateFlow()

    // Which debug dialog is open — a separate StateFlow (not local Compose state, not nested in
    // uiState) so previews can simulate the seed-scope sheet, same pattern as SectionsViewModel.
    private val _dialogState = MutableStateFlow<SettingsDialogState>(SettingsDialogState.None)
    val dialogState: StateFlow<SettingsDialogState> = _dialogState.asStateFlow()

    private val _uiAction = Channel<SettingsUiAction>(Channel.BUFFERED)
    val uiAction: Flow<SettingsUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnOrganizeSectionsClicked -> sendUiAction(SettingsUiAction.NavigateToSections)
            SettingsEvent.OnOrganizeTagsClicked -> sendUiAction(SettingsUiAction.NavigateToTags)
            SettingsEvent.OnItemsClicked -> sendUiAction(SettingsUiAction.NavigateToItems)
            SettingsEvent.OnSeedDatabaseClicked -> _dialogState.update { SettingsDialogState.SelectingSeedScope() }
            is SettingsEvent.OnSeedScopeSelected ->
                _dialogState.update { SettingsDialogState.SelectingSeedScope(event.scope) }
            SettingsEvent.OnSeedConfirmClicked -> handleSeedConfirm()
            SettingsEvent.OnSeedDialogDismissed -> _dialogState.update { SettingsDialogState.None }
            SettingsEvent.OnClearDatabaseClicked -> handleClearDatabase()
        }
    }

    private fun handleSeedConfirm() {
        val scope = (_dialogState.value as? SettingsDialogState.SelectingSeedScope)?.selectedScope ?: return
        viewModelScope.launch {
            runCatching { seedDatabase(scope) }
                .onSuccess {
                    _dialogState.update { SettingsDialogState.None }
                    // Back to Home so the freshly seeded panel/list is visible right away.
                    _uiAction.send(SettingsUiAction.ShowSnackbar(R.string.settings_debug_seed_success))
                    _uiAction.send(SettingsUiAction.NavigateBack)
                }
                .onFailure { _uiAction.send(SettingsUiAction.ShowError(it.message.orEmpty())) }
        }
    }

    private fun handleClearDatabase() = viewModelScope.launch {
        runCatching { clearDatabase() }
            .onSuccess {
                _uiAction.send(SettingsUiAction.ShowSnackbar(R.string.settings_debug_clear_success))
                _uiAction.send(SettingsUiAction.NavigateBack)
            }
            .onFailure { _uiAction.send(SettingsUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun sendUiAction(action: SettingsUiAction) = viewModelScope.launch {
        _uiAction.send(action)
    }
}
