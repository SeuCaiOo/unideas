package com.seucaio.unideas.feature.settings.viewmodel

import app.cash.turbine.test
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

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when created should expose Success with a disconnected backup status`() = runTest {
        val vm = SettingsViewModel()

        assertEquals(SettingsUiState.Success(BackupStatus.DISCONNECTED), vm.uiState.value)
    }

    @Test
    fun `when OnOrganizeSectionsClicked should navigate to sections`() = runTest {
        val vm = SettingsViewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnOrganizeSectionsClicked)
            assertEquals(SettingsUiAction.NavigateToSections, awaitItem())
        }
    }

    @Test
    fun `when OnOrganizeTagsClicked should navigate to tags`() = runTest {
        val vm = SettingsViewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnOrganizeTagsClicked)
            assertEquals(SettingsUiAction.NavigateToTags, awaitItem())
        }
    }

    @Test
    fun `when OnItemsClicked should navigate to items`() = runTest {
        val vm = SettingsViewModel()

        vm.uiAction.test {
            vm.onEvent(SettingsEvent.OnItemsClicked)
            assertEquals(SettingsUiAction.NavigateToItems, awaitItem())
        }
    }
}
