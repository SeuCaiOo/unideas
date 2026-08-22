package com.seucaio.unideas.feature.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.backup.domain.usecase.GoogleAuthUseCase
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
    private val setOnboardingSeenUseCase: SetOnboardingSeenUseCase,
    private val googleAuthUseCase: GoogleAuthUseCase,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState.Success).asStateFlow()

    // Which debug dialog is open — a separate StateFlow (not local Compose state, not nested in
    // uiState) so previews can simulate the seed-scope sheet, same pattern as SectionsViewModel.
    private val _dialogState = MutableStateFlow<SettingsDialogState>(SettingsDialogState.None)
    val dialogState: StateFlow<SettingsDialogState> = _dialogState.asStateFlow()

    private val _accountUiState = MutableStateFlow(resolveAccountState())
    val accountUiState: StateFlow<SettingsAccountUiState> = _accountUiState.asStateFlow()

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
            SettingsEvent.OnLogoutConfirmed -> handleLogoutConfirmed()
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

    private fun handleLogoutConfirmed() = viewModelScope.launch {
        clearDatabase()
        googleAuthUseCase.signOut()
        _accountUiState.update { SettingsAccountUiState() }
        setOnboardingSeenUseCase(false)
        _uiAction.send(SettingsUiAction.LogoutCompleted)
    }

    private fun resolveAccountState(): SettingsAccountUiState {
        val account = googleAuthUseCase.getSignedInAccount()
        return SettingsAccountUiState(
            isConnected = account != null,
            accountName = account?.displayName,
            accountEmail = account?.email,
        )
    }

    private fun sendUiAction(action: SettingsUiAction) = viewModelScope.launch {
        _uiAction.send(action)
    }
}
