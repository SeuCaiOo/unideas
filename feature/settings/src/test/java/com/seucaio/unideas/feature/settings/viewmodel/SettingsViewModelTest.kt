package com.seucaio.unideas.feature.settings.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.SeedScope
import com.seucaio.unideas.domain.usecase.settings.ClearDatabaseUseCase
import com.seucaio.unideas.domain.usecase.settings.SeedDatabaseUseCase
import com.seucaio.unideas.feature.settings.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @MockK
    private lateinit var seedDatabase: SeedDatabaseUseCase

    @MockK
    private lateinit var clearDatabase: ClearDatabaseUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SettingsViewModel(seedDatabase, clearDatabase)

    @Test
    fun `when created should expose Success with a disconnected backup status`() = runTest {
        val vm = viewModel()

        assertEquals(SettingsUiState.Success(BackupStatus.DISCONNECTED), vm.uiState.value)
    }

    @Test
    fun `when created should have no dialog open`() = runTest {
        val vm = viewModel()

        assertEquals(SettingsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnOrganizeSectionsClicked should navigate to sections`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnOrganizeSectionsClicked)
            assertEquals(SettingsUiAction.NavigateToSections, awaitItem())
        }
    }

    @Test
    fun `when OnOrganizeTagsClicked should navigate to tags`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnOrganizeTagsClicked)
            assertEquals(SettingsUiAction.NavigateToTags, awaitItem())
        }
    }

    @Test
    fun `when OnItemsClicked should navigate to items`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnItemsClicked)
            assertEquals(SettingsUiAction.NavigateToItems, awaitItem())
        }
    }

    @Test
    fun `when OnSeedDatabaseClicked should open the seed-scope sheet with no scope selected`() = runTest {
        val vm = viewModel()

        vm.onEvent(SettingsEvent.OnSeedDatabaseClicked)

        assertEquals(SettingsDialogState.SelectingSeedScope(null), vm.dialogState.value)
    }

    @Test
    fun `when OnSeedScopeSelected should update the selected scope in the open sheet`() = runTest {
        val vm = viewModel()

        vm.onEvent(SettingsEvent.OnSeedDatabaseClicked)
        vm.onEvent(SettingsEvent.OnSeedScopeSelected(SeedScope.FULL))

        assertEquals(SettingsDialogState.SelectingSeedScope(SeedScope.FULL), vm.dialogState.value)
    }

    @Test
    fun `when OnSeedConfirmClicked with no scope selected should not call the use case`() = runTest {
        val vm = viewModel()

        vm.onEvent(SettingsEvent.OnSeedDatabaseClicked)
        vm.onEvent(SettingsEvent.OnSeedConfirmClicked)

        coVerify(exactly = 0) { seedDatabase(any()) }
    }

    @Test
    fun `when OnSeedConfirmClicked succeeds should dismiss the sheet and show a success snackbar`() = runTest {
        coEvery { seedDatabase(SeedScope.FULL) } returns Unit
        val vm = viewModel()
        vm.onEvent(SettingsEvent.OnSeedDatabaseClicked)
        vm.onEvent(SettingsEvent.OnSeedScopeSelected(SeedScope.FULL))

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnSeedConfirmClicked)
            assertEquals(SettingsUiAction.ShowSnackbar(R.string.settings_debug_seed_success), awaitItem())
            assertEquals(SettingsUiAction.NavigateBack, awaitItem())
        }
        coVerify(exactly = 1) { seedDatabase(SeedScope.FULL) }
        assertEquals(SettingsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnSeedConfirmClicked fails should show the error message and keep the sheet open`() = runTest {
        coEvery { seedDatabase(SeedScope.BASIC) } throws IllegalStateException("boom")
        val vm = viewModel()
        vm.onEvent(SettingsEvent.OnSeedDatabaseClicked)
        vm.onEvent(SettingsEvent.OnSeedScopeSelected(SeedScope.BASIC))

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnSeedConfirmClicked)
            assertEquals(SettingsUiAction.ShowError("boom"), awaitItem())
        }
        assertEquals(SettingsDialogState.SelectingSeedScope(SeedScope.BASIC), vm.dialogState.value)
    }

    @Test
    fun `when OnSeedDialogDismissed should close the sheet`() = runTest {
        val vm = viewModel()
        vm.onEvent(SettingsEvent.OnSeedDatabaseClicked)

        vm.onEvent(SettingsEvent.OnSeedDialogDismissed)

        assertEquals(SettingsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnClearDatabaseClicked succeeds should show a success snackbar`() = runTest {
        coEvery { clearDatabase() } returns Unit
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnClearDatabaseClicked)
            assertEquals(SettingsUiAction.ShowSnackbar(R.string.settings_debug_clear_success), awaitItem())
            assertEquals(SettingsUiAction.NavigateBack, awaitItem())
        }
        coVerify(exactly = 1) { clearDatabase() }
    }

    @Test
    fun `when OnClearDatabaseClicked fails should show the error message`() = runTest {
        coEvery { clearDatabase() } throws IllegalStateException("boom")
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnClearDatabaseClicked)
            assertEquals(SettingsUiAction.ShowError("boom"), awaitItem())
        }
    }
}
