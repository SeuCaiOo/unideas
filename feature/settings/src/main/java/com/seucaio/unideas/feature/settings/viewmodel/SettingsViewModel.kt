package com.seucaio.unideas.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.seucaio.unideas.core.backup.domain.usecase.BackupUseCase
import com.seucaio.unideas.domain.usecase.onboarding.SetOnboardingSeenUseCase
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

class SettingsViewModel(
    private val seedDatabase: SeedDatabaseUseCase,
    private val clearDatabase: ClearDatabaseUseCase,
    private val backupUseCase: BackupUseCase,
    private val setOnboardingSeenUseCase: SetOnboardingSeenUseCase,
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
            is SettingsEvent.OnLogoutConfirmed -> handleLogoutConfirmed(event.account)
            SettingsEvent.OnAccountSignedOut -> handleAccountSignedOut()
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

    private fun handleLogoutConfirmed(account: GoogleSignInAccount) = viewModelScope.launch {
        backupUseCase.upload(account)
        clearDatabase()
        _uiAction.send(SettingsUiAction.SignOutRequested)
    }

    private fun handleAccountSignedOut() = viewModelScope.launch {
        setOnboardingSeenUseCase(false)
        _uiAction.send(SettingsUiAction.LogoutCompleted)
    }

    private fun sendUiAction(action: SettingsUiAction) = viewModelScope.launch {
        _uiAction.send(action)
    }
}
