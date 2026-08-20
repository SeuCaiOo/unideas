package com.seucaio.unideas.feature.items.ui.screens.config.sectionstags.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.section.SectionUseCase
import com.seucaio.unideas.domain.usecase.tag.TagUseCase
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
    private lateinit var sectionUseCase: SectionUseCase

    @MockK
    private lateinit var tagUseCase: TagUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { sectionUseCase.getAll() } returns flowOf(SectionStub.sections())
        every { tagUseCase.getAll() } returns flowOf(TagStub.tags())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SectionsTagsViewModel(sectionUseCase, tagUseCase)

    @Test
    fun `when the flows emit should combine sections and tags into uiState`() = runTest {
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
        coEvery { sectionUseCase.add("Trabalho") } returns Result.success(1L)

        vm.onEvent(SectionsTagsEvent.OnAddSectionClicked)
        vm.onEvent(SectionsTagsEvent.OnSectionCreateRequested("Trabalho"))

        coVerify(exactly = 1) { sectionUseCase.add("Trabalho") }
        assertEquals(SectionsTagsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnSectionCreateRequested fails with a blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { sectionUseCase.add("") } returns Result.failure(IllegalArgumentException("Name is required"))

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
        coEvery { tagUseCase.add("urgente") } returns Result.success(1L)

        vm.onEvent(SectionsTagsEvent.OnAddTagClicked)
        vm.onEvent(SectionsTagsEvent.OnTagCreateRequested("urgente"))

        coVerify(exactly = 1) { tagUseCase.add("urgente") }
        assertEquals(SectionsTagsDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnTagCreateRequested fails with a blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { tagUseCase.add("") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.uiAction.test {
            vm.onEvent(SectionsTagsEvent.OnTagCreateRequested(""))
            assertEquals(SectionsTagsUiAction.ShowSnackbar(R.string.item_config_tag_name_required), awaitItem())
        }
    }

    @Test
    fun `when the use case fails unexpectedly should emit ShowError with the exception message`() = runTest {
        val vm = viewModel()
        coEvery { sectionUseCase.add("Trabalho") } returns Result.failure(IllegalStateException("boom"))

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
