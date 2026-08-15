package com.seucaio.unideas.feature.home.features.home.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.model.outcome.CompletionResult
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.HomeUseCase
import com.seucaio.unideas.feature.home.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
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

/** Each test targets whichever of `filterState`/`itemsState`/`uiState` owns the behavior. */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @MockK
    private lateinit var homeUseCase: HomeUseCase

    @MockK
    private lateinit var getSectionsAndTags: GetSectionsAndTagsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { getSectionsAndTags() } returns SectionsAndTags(emptyList(), emptyList())
        every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(emptyList())
        every { homeUseCase.hasAnyItem() } returns flowOf(true)
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(homeUseCase, getSectionsAndTags)

    @Test
    fun `when OnTabChanged should switch the active tab and reload the tab list`() = runTest {
        val taskItems = listOf(ItemStub.task(id = 1L))
        val noteItems = listOf(ItemStub.note(id = 2L))
        every { homeUseCase.getItems(ItemType.TASK, null, emptyList()) } returns flowOf(taskItems)
        every { homeUseCase.getItems(ItemType.NOTE, null, emptyList()) } returns flowOf(noteItems)
        val vm = viewModel()

        vm.itemsState.test {
            assertEquals(taskItems, awaitItem().tabItems)
            vm.onEvent(HomeEvent.OnTabChanged(ItemType.NOTE))
            val state = awaitItem()
            assertEquals(noteItems, state.tabItems)
        }
        assertEquals(ItemType.NOTE, vm.filterState.value.activeTab)
    }

    @Test
    fun `when OnSectionFilterChanged should reload the tab list filtered by section`() = runTest {
        every { homeUseCase.getItems(ItemType.TASK, null, emptyList()) } returns flowOf(emptyList())
        every { homeUseCase.getItems(ItemType.TASK, 7L, emptyList()) } returns
            flowOf(listOf(ItemStub.task(id = 1L, sectionId = 7L)))
        val vm = viewModel()

        vm.itemsState.test {
            awaitItem()
            vm.onEvent(HomeEvent.OnSectionFilterChanged(7L))
            awaitItem()
            coVerify { homeUseCase.getItems(ItemType.TASK, 7L, emptyList()) }
        }
        assertEquals(7L, vm.filterState.value.sectionFilter)
    }

    @Test
    fun `when OnTagFilterToggled should reload the tab list filtered by the toggled tag`() = runTest {
        every { homeUseCase.getItems(ItemType.TASK, null, emptyList()) } returns flowOf(emptyList())
        every { homeUseCase.getItems(ItemType.TASK, null, listOf(9L)) } returns flowOf(listOf(ItemStub.task(id = 1L)))
        val vm = viewModel()

        vm.itemsState.test {
            awaitItem()
            vm.onEvent(HomeEvent.OnTagFilterToggled(9L))
            awaitItem()
        }
        assertEquals(setOf(9L), vm.filterState.value.tagFilters)
    }

    @Test
    fun `when OnTagFilterToggled twice for the same tag should clear the filter`() = runTest {
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnTagFilterToggled(9L))
        assertEquals(setOf(9L), vm.filterState.value.tagFilters)
        vm.onEvent(HomeEvent.OnTagFilterToggled(9L))
        assertEquals(emptySet<Long>(), vm.filterState.value.tagFilters)
    }

    @Test
    fun `when OnViewModeChanged should switch the view mode`() = runTest {
        val vm = viewModel()

        assertEquals(ItemsViewMode.LIST, vm.filterState.value.viewMode)
        vm.onEvent(HomeEvent.OnViewModeChanged(ItemsViewMode.GRID))
        assertEquals(ItemsViewMode.GRID, vm.filterState.value.viewMode)
    }

    @Test
    fun `when loading reference data succeeds should surface available sections and tags`() = runTest {
        val sections = listOf(Section(id = 1L, name = "Casa"))
        val tags = listOf(Tag(id = 1L, name = "Urgente"))
        coEvery { getSectionsAndTags() } returns SectionsAndTags(sections, tags)
        val vm = viewModel()

        assertEquals(sections, vm.filterState.value.availableSections)
        assertEquals(tags, vm.filterState.value.availableTags)
    }

    @Test
    fun `when OnSectionPinToggled succeeds should reload sections reflecting the new pin state`() = runTest {
        val section = Section(id = 1L, name = "Work", isPinned = false)
        val pinnedSection = section.copy(isPinned = true)
        coEvery { getSectionsAndTags() } returnsMany listOf(
            SectionsAndTags(listOf(section), emptyList()),
            SectionsAndTags(listOf(pinnedSection), emptyList()),
        )
        coEvery { homeUseCase.setSectionPinned(section.id, true) } returns Result.success(Unit)
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnSectionPinToggled(section.id, true))

        assertEquals(listOf(pinnedSection), vm.filterState.value.availableSections)
    }

    @Test
    fun `when OnSectionPinToggled fails should emit ShowError`() = runTest {
        coEvery { homeUseCase.setSectionPinned(1L, true) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnSectionPinToggled(1L, true))
            assertEquals(HomeUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnItemPinToggled succeeds should call HomeUseCase's setItemPinned`() = runTest {
        coEvery { homeUseCase.setItemPinned(1L, true) } returns Result.success(Unit)
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnItemPinToggled(1L, true))

        coVerify(exactly = 1) { homeUseCase.setItemPinned(1L, true) }
    }

    @Test
    fun `when OnItemPinToggled fails should emit ShowError`() = runTest {
        coEvery { homeUseCase.setItemPinned(1L, true) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnItemPinToggled(1L, true))
            assertEquals(HomeUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnCompleteClicked for a known item should call HomeUseCase's complete`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(listOf(item))
        coEvery { homeUseCase.complete(item, any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel()

        vm.itemsState.test { awaitItem() }
        vm.onEvent(HomeEvent.OnCompleteClicked(1L))

        coVerify(exactly = 1) { homeUseCase.complete(item, any()) }
    }

    @Test
    fun `when OnCompleteClicked fails should emit ShowError`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(listOf(item))
        coEvery { homeUseCase.complete(item, any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.itemsState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnCompleteClicked(1L))
            assertEquals(HomeUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when tab items span multiple sections should group them in section order with unsectioned last`() = runTest {
        val work = Section(id = 1L, name = "Work")
        val personal = Section(id = 2L, name = "Personal")
        coEvery { getSectionsAndTags() } returns SectionsAndTags(listOf(work, personal), emptyList())
        val personalItem = ItemStub.task(id = 1L, sectionId = personal.id)
        val workItem = ItemStub.task(id = 2L, sectionId = work.id)
        val unsectionedItem = ItemStub.task(id = 3L, sectionId = null)
        every { homeUseCase.getItems(ItemType.TASK, null, emptyList()) } returns
            flowOf(listOf(personalItem, workItem, unsectionedItem))
        val vm = viewModel()

        vm.itemsState.test {
            val state = awaitItem()
            assertEquals(
                listOf(
                    ItemSectionGroup(work.id, work.name, listOf(workItem)),
                    ItemSectionGroup(personal.id, personal.name, listOf(personalItem)),
                    ItemSectionGroup(sectionId = null, sectionName = null, items = listOf(unsectionedItem)),
                ),
                state.groupedTabItems,
            )
        }
    }

    @Test
    fun `when a section filter is active should still expose groupedTabItems for that section only`() = runTest {
        val work = Section(id = 7L, name = "Work")
        coEvery { getSectionsAndTags() } returns SectionsAndTags(listOf(work), emptyList())
        val item = ItemStub.task(id = 1L, sectionId = work.id)
        every { homeUseCase.getItems(ItemType.TASK, 7L, emptyList()) } returns flowOf(listOf(item))
        val vm = viewModel()

        vm.itemsState.test {
            awaitItem()
            vm.onEvent(HomeEvent.OnSectionFilterChanged(7L))
            val state = awaitItem()
            assertEquals(listOf(ItemSectionGroup(work.id, work.name, listOf(item))), state.groupedTabItems)
        }
    }

    @Test
    fun `when the tab items query throws should degrade to an empty list, not a screen error`() = runTest {
        every { homeUseCase.getItems(any(), any(), any()) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.itemsState.test {
            assertEquals(emptyList<Nothing>(), awaitItem().tabItems)
        }
    }

    @Test
    fun `when there are priority items should expose hasAnyPriorityItem as true`() = runTest {
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(listOf(ItemStub.overdueTask()))
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertEquals(true, state.hasAnyPriorityItem)
        }
    }

    @Test
    fun `when there are no priority items should expose hasAnyPriorityItem as false`() = runTest {
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertEquals(false, state.hasAnyPriorityItem)
        }
    }

    @Test
    fun `when getPriorityItems throws should degrade hasAnyPriorityItem to false, not a screen error`() = runTest {
        every { homeUseCase.getPriorityItems(any(), any()) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertEquals(false, state.hasAnyPriorityItem)
        }
    }

    @Test
    fun `when hasAnyItem throws should emit Error`() = runTest {
        every { homeUseCase.hasAnyItem() } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(HomeUiState.Error(R.string.home_load_error), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should retry and succeed`() = runTest {
        every { homeUseCase.hasAnyItem() } returnsMany
            listOf(flow { throw IllegalStateException("boom") }, flowOf(true))
        val vm = viewModel()

        vm.uiState.test {
            assertEquals(HomeUiState.Error(R.string.home_load_error), awaitItem())
            vm.onEvent(HomeEvent.OnRetryClicked)
            // Under UnconfinedTestDispatcher, Error -> Loading -> Success runs synchronously;
            // the transient Loading can be overwritten before Turbine observes it (StateFlow
            // conflation), so only the terminal state is asserted here.
            val state = awaitItem() as HomeUiState.Success
            assertEquals(true, state.hasAnyItem)
        }
    }

    @Test
    fun `when OnRefreshRequested should call HomeUseCase's refreshReminders and leave isRefreshing false`() = runTest {
        every { homeUseCase.refreshReminders() } returns Unit
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnRefreshRequested)

        verify(exactly = 1) { homeUseCase.refreshReminders() }
        assertEquals(false, vm.isRefreshing.value)
    }

    @Test
    fun `when OnItemClicked should emit NavigateToDetail with the item id`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnItemClicked(42L))
            assertEquals(HomeUiAction.NavigateToDetail(42L), awaitItem())
        }
    }

    @Test
    fun `when OnAddClicked should emit NavigateToAddItem with the chosen type`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnAddClicked(ItemType.NOTE))
            assertEquals(HomeUiAction.NavigateToAddItem(ItemType.NOTE), awaitItem())
        }
    }

    @Test
    fun `when OnItemLongPressed should select the item and enter selection mode`() = runTest {
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnItemLongPressed(1L))

        assertEquals(HomeMode.Selection(setOf(1L)), vm.homeMode.value)
    }

    @Test
    fun `when OnItemSelectionToggled twice for the same item should deselect it but stay in selection mode`() = runTest {
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnItemLongPressed(1L))
        vm.onEvent(HomeEvent.OnItemSelectionToggled(1L))

        assertEquals(HomeMode.Selection(emptySet()), vm.homeMode.value)
    }

    @Test
    fun `when OnItemSelectionToggled for a different item should add it to the selection`() = runTest {
        val vm = viewModel()

        vm.onEvent(HomeEvent.OnItemLongPressed(1L))
        vm.onEvent(HomeEvent.OnItemSelectionToggled(2L))

        assertEquals(HomeMode.Selection(setOf(1L, 2L)), vm.homeMode.value)
    }

    @Test
    fun `when OnItemClicked while in selection mode should toggle selection instead of navigating`() = runTest {
        val vm = viewModel()
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnItemClicked(2L))
            expectNoEvents()
        }
        assertEquals(HomeMode.Selection(setOf(1L, 2L)), vm.homeMode.value)
    }

    @Test
    fun `when OnSelectionCleared should clear the selection and exit selection mode`() = runTest {
        val vm = viewModel()
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))

        vm.onEvent(HomeEvent.OnSelectionCleared)

        assertEquals(HomeMode.Normal, vm.homeMode.value)
    }

    @Test
    fun `when OnSelectAllClicked should select every currently loaded item`() = runTest {
        val items = listOf(ItemStub.task(id = 1L), ItemStub.task(id = 2L), ItemStub.task(id = 3L))
        every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(items)
        val vm = viewModel()
        vm.itemsState.test { awaitItem() }
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))

        vm.onEvent(HomeEvent.OnSelectAllClicked)

        assertEquals(HomeMode.Selection(setOf(1L, 2L, 3L)), vm.homeMode.value)
    }

    @Test
    fun `when OnSelectAllClicked while every item is already selected should deselect all but stay in selection mode`() =
        runTest {
            val items = listOf(ItemStub.task(id = 1L), ItemStub.task(id = 2L))
            every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(items)
            val vm = viewModel()
            vm.itemsState.test { awaitItem() }
            vm.onEvent(HomeEvent.OnItemLongPressed(1L))
            vm.onEvent(HomeEvent.OnSelectAllClicked)

            vm.onEvent(HomeEvent.OnSelectAllClicked)

            assertEquals(HomeMode.Selection(emptySet()), vm.homeMode.value)
        }

    @Test
    fun `when OnGroupSelectAllClicked should select every item in that section only`() = runTest {
        val items = listOf(
            ItemStub.task(id = 1L, sectionId = 10L),
            ItemStub.task(id = 2L, sectionId = 10L),
            ItemStub.task(id = 3L, sectionId = 20L),
        )
        every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(items)
        val vm = viewModel()
        vm.itemsState.test { awaitItem() }
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))

        vm.onEvent(HomeEvent.OnGroupSelectAllClicked(10L))

        assertEquals(HomeMode.Selection(setOf(1L, 2L)), vm.homeMode.value)
    }

    @Test
    fun `when OnGroupSelectAllClicked while every item in the section is already selected should deselect just that section`() =
        runTest {
            val items = listOf(
                ItemStub.task(id = 1L, sectionId = 10L),
                ItemStub.task(id = 2L, sectionId = 10L),
                ItemStub.task(id = 3L, sectionId = 20L),
            )
            every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(items)
            val vm = viewModel()
            vm.itemsState.test { awaitItem() }
            vm.onEvent(HomeEvent.OnItemLongPressed(3L))
            vm.onEvent(HomeEvent.OnGroupSelectAllClicked(10L))

            vm.onEvent(HomeEvent.OnGroupSelectAllClicked(10L))

            assertEquals(HomeMode.Selection(setOf(3L)), vm.homeMode.value)
        }

    @Test
    fun `when OnGroupSelectAllClicked for the unsectioned group should select items with a null sectionId`() = runTest {
        val items = listOf(
            ItemStub.task(id = 1L, sectionId = null),
            ItemStub.task(id = 2L, sectionId = 10L),
        )
        every { homeUseCase.getItems(any(), any(), any()) } returns flowOf(items)
        val vm = viewModel()
        vm.itemsState.test { awaitItem() }
        vm.onEvent(HomeEvent.OnItemLongPressed(2L))

        vm.onEvent(HomeEvent.OnGroupSelectAllClicked(null))

        assertEquals(HomeMode.Selection(setOf(1L, 2L)), vm.homeMode.value)
    }

    @Test
    fun `when OnDeleteSelectedClicked should open the delete confirmation dialog without deleting`() = runTest {
        val vm = viewModel()
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))

        vm.onEvent(HomeEvent.OnDeleteSelectedClicked)

        assertEquals(HomeDialogState.DeleteSelectedConfirm, vm.dialogState.value)
        coVerify(exactly = 0) { homeUseCase.deleteItems(any()) }
    }

    @Test
    fun `when OnDeleteDialogDismissed should close the dialog and keep the selection`() = runTest {
        val vm = viewModel()
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))
        vm.onEvent(HomeEvent.OnDeleteSelectedClicked)

        vm.onEvent(HomeEvent.OnDeleteDialogDismissed)

        assertEquals(HomeDialogState.None, vm.dialogState.value)
        assertEquals(HomeMode.Selection(setOf(1L)), vm.homeMode.value)
    }

    @Test
    fun `when OnDeleteSelectedConfirmClicked succeeds should delete the selected items, close the dialog and exit selection mode`() =
        runTest {
            coEvery { homeUseCase.deleteItems(listOf(1L, 2L)) } returns Result.success(Unit)
            val vm = viewModel()
            vm.onEvent(HomeEvent.OnItemLongPressed(1L))
            vm.onEvent(HomeEvent.OnItemSelectionToggled(2L))
            vm.onEvent(HomeEvent.OnDeleteSelectedClicked)

            vm.onEvent(HomeEvent.OnDeleteSelectedConfirmClicked)

            coVerify(exactly = 1) { homeUseCase.deleteItems(listOf(1L, 2L)) }
            assertEquals(HomeDialogState.None, vm.dialogState.value)
            assertEquals(HomeMode.Normal, vm.homeMode.value)
        }

    @Test
    fun `when OnDeleteSelectedConfirmClicked fails should emit ShowError and keep the selection`() = runTest {
        coEvery { homeUseCase.deleteItems(listOf(1L)) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()
        vm.onEvent(HomeEvent.OnItemLongPressed(1L))
        vm.onEvent(HomeEvent.OnDeleteSelectedClicked)

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnDeleteSelectedConfirmClicked)
            assertEquals(HomeUiAction.ShowError("boom"), awaitItem())
        }
        assertEquals(HomeMode.Selection(setOf(1L)), vm.homeMode.value)
    }
}
