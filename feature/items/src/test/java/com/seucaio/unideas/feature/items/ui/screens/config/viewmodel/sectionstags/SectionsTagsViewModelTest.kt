package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel.sectionstags

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.SectionsAndTagsUseCase
import com.seucaio.unideas.feature.items.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class SectionsTagsViewModelTest {

    @MockK
    private lateinit var sectionsAndTagsUseCase: SectionsAndTagsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sectionsAndTagsUseCase.getAll() } returns
            flowOf(SectionsAndTags(SectionStub.sections(), TagStub.tags()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SectionsTagsViewModel(sectionsAndTagsUseCase)

    @Test
    fun `when the flow emits should combine sections and tags into uiState`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(SectionsTagsUiState(SectionStub.sections(), TagStub.tags()), awaitItem())
        }
    }

    @Test
    fun `when OnAddSectionClicked should show the AddSection dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(SectionsTagsEvent.OnAddSectionClicked)

        assertEquals(SectionsTagsDialogState.AddSection, vm.dialogState.value)
    }

    @Test
    fun `when OnAddTagClicked should show the AddTag dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(SectionsTagsEvent.OnAddTagClicked)

        assertEquals(SectionsTagsDialogState.AddTag, vm.dialogState.value)
    }

    @Test
    fun `when OnSectionCreateRequested succeeds should call the use case and dismiss the dialog`() = runTest {
        val vm = viewModel()
        coEvery { sectionsAndTagsUseCase.addSection("Trabalho") } returns Result.success(1L)

        vm.onEvent(SectionsTagsEvent.OnAddSectionClicked)
        vm.onEvent(SectionsTagsEvent.OnSectionCreateRequested("Trabalho"))

        coVerify(exactly = 1) { sectionsAndTagsUseCase.addSection("Trabalho") }
        assertEquals(SectionsTagsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnSectionCreateRequested fails with a blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { sectionsAndTagsUseCase.addSection("") } returns
            Result.failure(IllegalArgumentException("Name is required"))

        vm.uiAction.test {
            vm.onEvent(SectionsTagsEvent.OnSectionCreateRequested(""))
            assertEquals(
                SectionsTagsUiAction.ShowSnackbar(R.string.item_config_section_name_required),
                awaitItem(),
            )
        }
    }

    @Test
    fun `when OnTagCreateRequested succeeds should call the use case and dismiss the dialog`() = runTest {
        val vm = viewModel()
        coEvery { sectionsAndTagsUseCase.addTag("urgente") } returns Result.success(1L)

        vm.onEvent(SectionsTagsEvent.OnAddTagClicked)
        vm.onEvent(SectionsTagsEvent.OnTagCreateRequested("urgente"))

        coVerify(exactly = 1) { sectionsAndTagsUseCase.addTag("urgente") }
        assertEquals(SectionsTagsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnTagCreateRequested fails with a blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { sectionsAndTagsUseCase.addTag("") } returns
            Result.failure(IllegalArgumentException("Name is required"))

        vm.uiAction.test {
            vm.onEvent(SectionsTagsEvent.OnTagCreateRequested(""))
            assertEquals(SectionsTagsUiAction.ShowSnackbar(R.string.item_config_tag_name_required), awaitItem())
        }
    }

    @Test
    fun `when the use case fails unexpectedly should emit ShowError with the exception message`() = runTest {
        val vm = viewModel()
        coEvery { sectionsAndTagsUseCase.addSection("Trabalho") } returns
            Result.failure(IllegalStateException("boom"))

        vm.uiAction.test {
            vm.onEvent(SectionsTagsEvent.OnSectionCreateRequested("Trabalho"))
            assertEquals(SectionsTagsUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnDialogDismissed should hide the dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(SectionsTagsEvent.OnAddSectionClicked)
        vm.onEvent(SectionsTagsEvent.OnDialogDismissed)

        assertEquals(SectionsTagsDialogState.None, vm.dialogState.value)
    }
}
