package com.seucaio.unideas.feature.home.features.panel.viewmodel

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

/**
 * #102 (2026-07-22): [HomeViewModel] exposes [HomeViewModel.filterState]/[HomeViewModel.itemsState]/
 * [HomeViewModel.uiState] as three independent `StateFlow`s — tests target whichever one actually
 * owns the behavior under test, instead of casting a single combined `uiState` everywhere.
 * [ItemsState] carries no load/error of its own (a query failure just degrades to an empty list);
 * only [HomeUiState] (driven by `hasAnyItem()`) can go [HomeUiState.Error].
 */
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
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(emptyList())
        every { homeUseCase.hasAnyItem() } returns flowOf(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = HomeViewModel(homeUseCase, getSectionsAndTags)

    @Test
    fun `when the panel does not exceed the limit should not show the see-all button`() = runTest {
        val items = (1..3).map { ItemStub.overdueTask(id = it.toLong()) }
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(items)
        val vm = viewModel()

        vm.itemsState.test {
            val state = awaitItem()
            assertEquals(items, state.priorityItems)
            assertEquals(false, state.showSeeAllButton)
        }
    }

    @Test
    fun `when the panel exceeds the limit should cap it and show the see-all button`() = runTest {
        val items = (1..6).map { ItemStub.overdueTask(id = it.toLong()) }
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(items)
        val vm = viewModel()

        vm.itemsState.test {
            val state = awaitItem()
            assertEquals(items.take(5), state.priorityItems)
            assertEquals(true, state.showSeeAllButton)
        }
    }

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
    fun `when OnTabChanged should not restart the priority panel query`() = runTest {
        every { homeUseCase.getItems(ItemType.TASK, null, emptyList()) } returns flowOf(emptyList())
        every { homeUseCase.getItems(ItemType.NOTE, null, emptyList()) } returns flowOf(emptyList())
        val vm = viewModel()

        vm.itemsState.test {
            awaitItem()
            vm.onEvent(HomeEvent.OnTabChanged(ItemType.NOTE))
            awaitItem()
        }

        verify(exactly = 1) { homeUseCase.getPriorityItems(any(), any()) }
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
    fun `when OnCompleteClicked for a known item should call HomeUseCase's complete`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(listOf(item))
        coEvery { homeUseCase.complete(item, any()) } returns Result.success(CompletionResult.Completed)
        val vm = viewModel()

        vm.itemsState.test { awaitItem() }
        vm.onEvent(HomeEvent.OnCompleteClicked(1L))

        coVerify(exactly = 1) { homeUseCase.complete(item, any()) }
    }

    @Test
    fun `when OnCompleteClicked fails should emit ShowError`() = runTest {
        val item = ItemStub.task(id = 1L)
        every { homeUseCase.getPriorityItems(any(), any()) } returns flowOf(listOf(item))
        coEvery { homeUseCase.complete(item, any()) } returns Result.failure(IllegalStateException("boom"))
        val vm = viewModel()

        vm.itemsState.test { awaitItem() }

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnCompleteClicked(1L))
            assertEquals(HomeUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when the user has no items anywhere should reflect hasAnyItem as false`() = runTest {
        every { homeUseCase.hasAnyItem() } returns flowOf(false)
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem() as HomeUiState.Success
            assertEquals(false, state.hasAnyItem)
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
    fun `when the priority items query throws should degrade to an empty list, not a screen error`() = runTest {
        every { homeUseCase.getPriorityItems(any(), any()) } returns flow { throw IllegalStateException("boom") }
        val vm = viewModel()

        vm.itemsState.test {
            assertEquals(emptyList<Nothing>(), awaitItem().priorityItems)
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
            assertEquals(HomeUiState.Loading, awaitItem())
            val state = awaitItem() as HomeUiState.Success
            assertEquals(true, state.hasAnyItem)
        }
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
    fun `when OnAddClicked should emit NavigateToForm with the chosen type`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnAddClicked(ItemType.NOTE))
            assertEquals(HomeUiAction.NavigateToForm(ItemType.NOTE), awaitItem())
        }
    }

    @Test
    fun `when OnSeeAllClicked should emit NavigateToAllPriorities`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnSeeAllClicked)
            assertEquals(HomeUiAction.NavigateToAllPriorities, awaitItem())
        }
    }

    @Test
    fun `when OnSettingsClicked should emit NavigateToSettings`() = runTest {
        val vm = viewModel()

        vm.uiAction.test {
            vm.onEvent(HomeEvent.OnSettingsClicked)
            assertEquals(HomeUiAction.NavigateToSettings, awaitItem())
        }
    }
}
