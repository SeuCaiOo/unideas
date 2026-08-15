package com.seucaio.unideas.feature.items.ui.screens.config.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.ReminderWarning
import com.seucaio.unideas.domain.model.SectionsAndTags
import com.seucaio.unideas.domain.stub.ItemStub
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.GetSectionsAndTagsUseCase
import com.seucaio.unideas.domain.usecase.item.ItemFormUseCase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ItemConfigViewModelTest {

    @MockK
    private lateinit var itemFormUseCase: ItemFormUseCase

    @MockK
    private lateinit var getSectionsAndTags: GetSectionsAndTagsUseCase

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        coEvery { getSectionsAndTags() } returns SectionsAndTags(SectionStub.sections(), TagStub.tags())
        coEvery { itemFormUseCase.edit(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(itemId: Long = 1L) =
        ItemConfigViewModel(itemId = itemId, itemFormUseCase = itemFormUseCase, getSectionsAndTags = getSectionsAndTags)

    @Test
    fun `when loading a task should expose its recurrence and reminder fields`() = runTest {
        val task = ItemStub.task(
            id = 1L,
            recurrence = Recurrence.Weekly,
            reminderWarning = ReminderWarning.DaysBefore(2)
        )
        every { itemFormUseCase.get(1L) } returns flowOf(task)
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals(ItemType.TASK, state.type)
            assertTrue(state.hasReminder)
            assertEquals(Recurrence.Weekly, state.recurrence)
            assertEquals(ReminderWarning.DaysBefore(2), state.reminderWarning)
        }
    }

    @Test
    fun `when loading fails should mark loadFailed`() = runTest {
        every { itemFormUseCase.get(1L) } returns flowOf(null)
        val vm = viewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(false, state.isLoading)
            assertTrue(state.loadFailed)
        }
    }

    @Test
    fun `when editing a note should accept recurrence and due date same as a task`() = runTest {
        val note = ItemStub.note(id = 2L)
        every { itemFormUseCase.get(2L) } returns flowOf(note)
        val vm = viewModel(itemId = 2L)

        vm.uiState.test {
            awaitItem()

            vm.onEvent(ItemConfigEvent.OnReminderToggled(true))
            awaitItem()
            vm.onEvent(ItemConfigEvent.OnRecurrenceChanged(Recurrence.Daily))
            val configured = awaitItem()

            assertEquals(ItemType.NOTE, configured.type)
            assertEquals(Recurrence.Daily, configured.recurrence)
        }

        coVerify {
            itemFormUseCase.edit(match { it.type == ItemType.NOTE && it.recurrence == Recurrence.Daily })
        }
    }

    @Test
    fun `field events auto-save through ItemFormUseCase's edit`() = runTest {
        val task = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(task)
        val vm = viewModel()

        vm.uiState.test {
            awaitItem()
            vm.onEvent(ItemConfigEvent.OnDueTimeChanged(LocalTime.of(9, 0)))
            awaitItem()
        }

        coVerify { itemFormUseCase.edit(match { it.dueTime == LocalTime.of(9, 0) }) }
    }

    @Test
    fun `OnChangeTypeClicked opens the confirm dialog without changing anything yet`() = runTest {
        val task = ItemStub.task(id = 1L, recurrence = Recurrence.Weekly)
        every { itemFormUseCase.get(1L) } returns flowOf(task)
        val vm = viewModel()
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemConfigEvent.OnChangeTypeClicked(ItemType.NOTE))

        vm.dialogState.test {
            val dialog = awaitItem()
            assertTrue(dialog is ItemConfigDialogState.TypeSwitchConfirm)
            assertEquals(ItemType.NOTE, (dialog as ItemConfigDialogState.TypeSwitchConfirm).newType)
        }
        coVerify(exactly = 0) { itemFormUseCase.edit(any()) }
    }

    @Test
    fun `OnDialogDismissed cancels the type switch without persisting`() = runTest {
        val task = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(task)
        val vm = viewModel()
        vm.uiState.test { awaitItem() }
        vm.onEvent(ItemConfigEvent.OnChangeTypeClicked(ItemType.NOTE))

        vm.onEvent(ItemConfigEvent.OnDialogDismissed)

        vm.dialogState.test {
            assertEquals(ItemConfigDialogState.None, awaitItem())
        }
        coVerify(exactly = 0) { itemFormUseCase.edit(any()) }
    }

    @Test
    fun `confirming the type switch resets every heavy field, even when the target type could keep them`() =
        runTest {
            val task = ItemStub.task(
                id = 1L,
                title = "Estudar inglês",
                description = "Toda semana",
                sectionId = SectionStub.sections().first().id,
                tags = listOf(TagStub.tags().first()),
                recurrence = Recurrence.Weekly,
                dueTime = LocalTime.of(19, 0),
                reminderWarning = ReminderWarning.DaysBefore(1),
            )
            every { itemFormUseCase.get(1L) } returns flowOf(task)
            val vm = viewModel()
            vm.uiState.test { awaitItem() }

            vm.onEvent(ItemConfigEvent.OnChangeTypeClicked(ItemType.NOTE))
            vm.onEvent(ItemConfigEvent.OnTypeSwitchConfirmClicked)

            val editedSlot = slot<Item>()
            coVerify { itemFormUseCase.edit(capture(editedSlot)) }
            val edited = editedSlot.captured
            assertEquals(ItemType.NOTE, edited.type)
            assertNull(edited.dueDate)
            assertNull(edited.dueTime)
            assertEquals(Recurrence.None, edited.recurrence)
            assertEquals(ReminderWarning.None, edited.reminderWarning)
            // title/description/section/tags are never touched by the type-switch guardrail.
            assertEquals("Estudar inglês", edited.title)
            assertEquals("Toda semana", edited.description)
            assertEquals(task.sectionId, edited.sectionId)
            assertEquals(task.tags, edited.tags)

            vm.uiState.test {
                val state = awaitItem()
                assertEquals(ItemType.NOTE, state.type)
                assertEquals(false, state.hasReminder)
                assertNull(state.dueDate)
                assertEquals(Recurrence.None, state.recurrence)
            }
            vm.dialogState.test {
                assertEquals(ItemConfigDialogState.None, awaitItem())
            }
        }

    @Test
    fun `type switch never touches an item's completion history — no such dependency exists to call`() = runTest {
        // ItemConfigViewModel only depends on ItemFormUseCase (Item table) — it has no reference to
        // ItemCompletionHistory at all, so a type switch structurally cannot delete/mutate it; the
        // rows survive via the FK to the (never deleted) item, same as any other item edit.
        val task = ItemStub.task(id = 1L)
        every { itemFormUseCase.get(1L) } returns flowOf(task)
        val vm = viewModel()
        vm.uiState.test { awaitItem() }

        vm.onEvent(ItemConfigEvent.OnChangeTypeClicked(ItemType.NOTE))
        vm.onEvent(ItemConfigEvent.OnTypeSwitchConfirmClicked)

        coVerify(exactly = 1) { itemFormUseCase.edit(any()) }
        coVerify(exactly = 0) { itemFormUseCase.delete(any()) }
    }
}
