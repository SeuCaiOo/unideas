package com.seucaio.unideas.feature.sections.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
import com.seucaio.unideas.feature.sections.R
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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

    private val getSections: GetSectionsUseCase = mockk()
    private val addSection: AddSectionUseCase = mockk()
    private val renameSection: RenameSectionUseCase = mockk()
    private val deleteSection: DeleteSectionUseCase = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): SectionsViewModel {
        every { getSections() } returns flowOf(SectionStub.sections())
        return SectionsViewModel(getSections, addSection, renameSection, deleteSection)
    }

    @Test
    fun `uiState emits Success with the sections from the flow`() = runTest {
        val sections = SectionStub.sections()
        every { getSections() } returns flowOf(sections)
        val vm = SectionsViewModel(getSections, addSection, renameSection, deleteSection)

        vm.uiState.test {
            assertEquals(SectionsUiState.Success(sections), awaitItem())
        }
    }

    @Test
    fun `uiState emits Error when the flow throws`() = runTest {
        every { getSections() } returns flow { throw IllegalStateException("boom") }
        val vm = SectionsViewModel(getSections, addSection, renameSection, deleteSection)

        vm.uiState.test {
            assertEquals(SectionsUiState.Error(R.string.sections_load_error), awaitItem())
        }
    }

    @Test
    fun `OnAddClicked happy path calls the use case without emitting an action`() = runTest {
        val vm = viewModel()
        coEvery { addSection.invoke("Trabalho") } returns Result.success(1L)

        vm.onEvent(SectionsEvent.OnAddClicked("Trabalho"))

        coVerify(exactly = 1) { addSection.invoke("Trabalho") }
    }

    @Test
    fun `OnAddClicked with blank name emits a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { addSection.invoke("") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.action.test {
            vm.onEvent(SectionsEvent.OnAddClicked(""))
            assertEquals(SectionsUiAction.ShowSnackbar(R.string.section_name_required), awaitItem())
        }
    }

    @Test
    fun `OnRenameClicked happy path calls the use case`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section(name = "renomeada")
        coEvery { renameSection.invoke(section) } returns Result.success(Unit)

        vm.onEvent(SectionsEvent.OnRenameClicked(SectionStub.section(), "renomeada"))

        coVerify(exactly = 1) { renameSection.invoke(section) }
    }

    @Test
    fun `OnRenameClicked with blank name emits a name-required snackbar`() = runTest {
        val vm = viewModel()
        val section = SectionStub.section(name = "")
        coEvery { renameSection.invoke(section) } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.action.test {
            vm.onEvent(SectionsEvent.OnRenameClicked(SectionStub.section(), ""))
            assertEquals(SectionsUiAction.ShowSnackbar(R.string.section_name_required), awaitItem())
        }
    }

    @Test
    fun `OnDeleteClicked blocked by linked items emits a snackbar with the count`() = runTest {
        val vm = viewModel()
        coEvery { deleteSection.invoke(1L) } returns Result.success(DeletionStatus.BlockedByLinkedItems(3))

        vm.action.test {
            vm.onEvent(SectionsEvent.OnDeleteClicked(1L))
            assertEquals(SectionsUiAction.ShowSnackbar(R.string.section_delete_blocked, listOf(3)), awaitItem())
        }
    }

    @Test
    fun `OnDeleteClicked completed does not emit an action`() = runTest {
        val vm = viewModel()
        coEvery { deleteSection.invoke(1L) } returns Result.success(DeletionStatus.Deleted)

        vm.onEvent(SectionsEvent.OnDeleteClicked(1L))

        coVerify(exactly = 1) { deleteSection.invoke(1L) }
    }

    @Test
    fun `unexpected repository failure emits ShowError with the exception message`() = runTest {
        val vm = viewModel()
        coEvery { deleteSection.invoke(1L) } returns Result.failure(IllegalStateException("boom"))

        vm.action.test {
            vm.onEvent(SectionsEvent.OnDeleteClicked(1L))
            assertEquals(SectionsUiAction.ShowError("boom"), awaitItem())
        }
    }
}
