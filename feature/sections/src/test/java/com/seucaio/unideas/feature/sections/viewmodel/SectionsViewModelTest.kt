package com.seucaio.unideas.feature.sections.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.feature.sections.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SectionsViewModelTest {

    @MockK
    private lateinit var sectionUseCase: SectionUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): SectionsViewModel {
        every { sectionUseCase.getAll() } returns flowOf(SectionStub.sections())
        return SectionsViewModel(sectionUseCase)
    }

    @Test
    fun `when the flow emits sections should update uiState to Success`() = runTest {
        val sections = SectionStub.sections()
        every { sectionUseCase.getAll() } returns flowOf(sections)
        val vm = SectionsViewModel(sectionUseCase)

        vm.uiState.test {
            assertEquals(SectionsUiState.Success(sections), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { sectionUseCase.getAll() } returns flow { throw IllegalStateException("boom") }
        val vm = SectionsViewModel(sectionUseCase)

        vm.uiState.test {
            assertEquals(SectionsUiState.Error(R.string.sections_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should re-fetch and update uiState to Success`() = runTest {
        val sections = SectionStub.sections()
        every { sectionUseCase.getAll() } returnsMany listOf(
            flow { throw IllegalStateException("boom") },
            flowOf(sections),
        )
        val vm = SectionsViewModel(sectionUseCase)

        vm.uiState.test {
            assertEquals(SectionsUiState.Error(R.string.sections_load_error), awaitItem())
            vm.onEvent(SectionsEvent.OnRetryClicked)
            assertEquals(SectionsUiState.Success(sections), awaitItem())
        }
    }

    @Test
    fun `when OnAddClicked should show the Add dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(SectionsEvent.OnAddClicked)

        assertEquals(SectionsDialogState.Add, vm.dialogState.value)
    }

    @Test
    fun `when OnAddConfirmClicked with a valid name should call the use case and dismiss the dialog`() = runTest {
        val vm = viewModel()
        coEvery { sectionUseCase.add("Trabalho") } returns Result.success(1L)

        vm.onEvent(SectionsEvent.OnAddClicked)
        vm.onEvent(SectionsEvent.OnAddConfirmClicked("Trabalho"))

        coVerify(exactly = 1) { sectionUseCase.add("Trabalho") }
        assertEquals(SectionsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnAddConfirmClicked with blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { sectionUseCase.add("") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.uiAction.test {
            vm.onEvent(SectionsEvent.OnAddConfirmClicked(""))
            assertEquals(SectionsUiAction.ShowSnackbar(R.string.section_name_required), awaitItem())
        }
    }

    @Test
    fun `when OnRenameClicked should show the Rename dialog with the section`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section()

        vm.onEvent(SectionsEvent.OnRenameClicked(section))

        assertEquals(SectionsDialogState.Rename(section), vm.dialogState.value)
    }

    @Test
    fun `when OnRenameConfirmClicked with a valid name should call the use case`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section()
        val renamed = section.copy(name = "renomeada")
        coEvery { sectionUseCase.rename(renamed) } returns Result.success(Unit)

        vm.onEvent(SectionsEvent.OnRenameClicked(section))
        vm.onEvent(SectionsEvent.OnRenameConfirmClicked("renomeada"))

        coVerify(exactly = 1) { sectionUseCase.rename(renamed) }
    }

    @Test
    fun `when OnRenameConfirmClicked with blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section()
        val blank = section.copy(name = "")
        coEvery { sectionUseCase.rename(blank) } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.onEvent(SectionsEvent.OnRenameClicked(section))

        vm.uiAction.test {
            vm.onEvent(SectionsEvent.OnRenameConfirmClicked(""))
            assertEquals(SectionsUiAction.ShowSnackbar(R.string.section_name_required), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteClicked should show the Delete dialog with the section`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section()

        vm.onEvent(SectionsEvent.OnDeleteClicked(section))

        assertEquals(SectionsDialogState.Delete(section), vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteConfirmClicked is blocked by linked items should emit a snackbar with the count`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section(id = 1L)
        coEvery { sectionUseCase.delete(1L) } returns Result.success(DeletionStatus.BlockedByLinkedItems(3))

        vm.onEvent(SectionsEvent.OnDeleteClicked(section))

        vm.uiAction.test {
            vm.onEvent(SectionsEvent.OnDeleteConfirmClicked)
            assertEquals(SectionsUiAction.ShowSnackbar(R.string.section_delete_blocked, listOf(3)), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteConfirmClicked completes should not emit an action`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section(id = 1L)
        coEvery { sectionUseCase.delete(1L) } returns Result.success(DeletionStatus.Deleted)

        vm.onEvent(SectionsEvent.OnDeleteClicked(section))
        vm.onEvent(SectionsEvent.OnDeleteConfirmClicked)

        coVerify(exactly = 1) { sectionUseCase.delete(1L) }
    }

    @Test
    fun `when the repository fails unexpectedly should emit ShowError with the exception message`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section(id = 1L)
        coEvery { sectionUseCase.delete(1L) } returns Result.failure(IllegalStateException("boom"))

        vm.onEvent(SectionsEvent.OnDeleteClicked(section))

        vm.uiAction.test {
            vm.onEvent(SectionsEvent.OnDeleteConfirmClicked)
            assertEquals(SectionsUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnDialogDismissed should hide the dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(SectionsEvent.OnAddClicked)
        vm.onEvent(SectionsEvent.OnDialogDismissed)

        assertEquals(SectionsDialogState.None, vm.dialogState.value)
    }
}
