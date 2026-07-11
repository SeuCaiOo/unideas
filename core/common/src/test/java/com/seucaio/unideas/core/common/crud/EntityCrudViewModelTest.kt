package com.seucaio.unideas.core.common.crud

import app.cash.turbine.test
import com.seucaio.unideas.domain.model.outcome.DeletionStatus
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

private data class FakeItem(val id: Long, val name: String)

private const val LOAD_ERROR_RES = 1
private const val NAME_REQUIRED_RES = 2
private const val DELETE_BLOCKED_RES = 3

@OptIn(ExperimentalCoroutinesApi::class)
class EntityCrudViewModelTest {

    @MockK
    private lateinit var operations: EntityCrudOperations<FakeItem>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): EntityCrudViewModel<FakeItem> {
        every { operations.getAll() } returns flowOf(listOf(FakeItem(1L, "Work")))
        return EntityCrudViewModel(operations, LOAD_ERROR_RES, NAME_REQUIRED_RES, DELETE_BLOCKED_RES)
    }

    @Test
    fun `when the flow emits items should update uiState to Success`() = runTest {
        val items = listOf(FakeItem(1L, "Work"), FakeItem(2L, "Home"))
        every { operations.getAll() } returns flowOf(items)
        val vm = EntityCrudViewModel(operations, LOAD_ERROR_RES, NAME_REQUIRED_RES, DELETE_BLOCKED_RES)

        vm.uiState.test {
            assertEquals(EntityCrudUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when the flow throws should emit Error`() = runTest {
        every { operations.getAll() } returns flow { throw IllegalStateException("boom") }
        val vm = EntityCrudViewModel(operations, LOAD_ERROR_RES, NAME_REQUIRED_RES, DELETE_BLOCKED_RES)

        vm.uiState.test {
            assertEquals(EntityCrudUiState.Error(LOAD_ERROR_RES), awaitItem())
        }
    }

    @Test
    fun `when OnRetryClicked after an error should re-fetch and update uiState to Success`() = runTest {
        val items = listOf(FakeItem(1L, "Work"))
        every { operations.getAll() } returnsMany listOf(
            flow { throw IllegalStateException("boom") },
            flowOf(items),
        )
        val vm = EntityCrudViewModel(operations, LOAD_ERROR_RES, NAME_REQUIRED_RES, DELETE_BLOCKED_RES)

        vm.uiState.test {
            assertEquals(EntityCrudUiState.Error(LOAD_ERROR_RES), awaitItem())
            vm.onEvent(EntityEvent.OnRetryClicked)
            assertEquals(EntityCrudUiState.Success(items), awaitItem())
        }
    }

    @Test
    fun `when OnAddClicked should show the Add dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(EntityEvent.OnAddClicked)

        assertEquals(EntityDialogState.Add, vm.dialogState.value)
    }

    @Test
    fun `when OnAddConfirmClicked with a valid name should call the operations and dismiss the dialog`() = runTest {
        val vm = viewModel()
        coEvery { operations.add("Trabalho") } returns Result.success(1L)

        vm.onEvent(EntityEvent.OnAddClicked)
        vm.onEvent(EntityEvent.OnAddConfirmClicked("Trabalho"))

        coVerify(exactly = 1) { operations.add("Trabalho") }
        assertEquals(EntityDialogState.None, vm.dialogState.value)
    }

    @Test
    fun `when OnAddConfirmClicked with blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        coEvery { operations.add("") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.uiAction.test {
            vm.onEvent(EntityEvent.OnAddConfirmClicked(""))
            assertEquals(EntityUiAction.ShowSnackbar(NAME_REQUIRED_RES), awaitItem())
        }
    }

    @Test
    fun `when OnRenameClicked should show the Rename dialog with the item`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")

        vm.onEvent(EntityEvent.OnRenameClicked(item))

        assertEquals(EntityDialogState.Rename(item), vm.dialogState.value)
    }

    @Test
    fun `when OnRenameConfirmClicked with a valid name should call the operations`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")
        coEvery { operations.rename(item, "renomeada") } returns Result.success(Unit)

        vm.onEvent(EntityEvent.OnRenameClicked(item))
        vm.onEvent(EntityEvent.OnRenameConfirmClicked("renomeada"))

        coVerify(exactly = 1) { operations.rename(item, "renomeada") }
    }

    @Test
    fun `when OnRenameConfirmClicked with blank name should emit a name-required snackbar`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")
        coEvery { operations.rename(item, "") } returns Result.failure(IllegalArgumentException("Name is required"))

        vm.onEvent(EntityEvent.OnRenameClicked(item))

        vm.uiAction.test {
            vm.onEvent(EntityEvent.OnRenameConfirmClicked(""))
            assertEquals(EntityUiAction.ShowSnackbar(NAME_REQUIRED_RES), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteClicked should show the Delete dialog with the item`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")

        vm.onEvent(EntityEvent.OnDeleteClicked(item))

        assertEquals(EntityDialogState.Delete(item), vm.dialogState.value)
    }

    @Test
    fun `when OnDeleteConfirmClicked is blocked by linked items should emit a snackbar with the count`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")
        coEvery { operations.delete(item) } returns Result.success(DeletionStatus.BlockedByLinkedItems(3))

        vm.onEvent(EntityEvent.OnDeleteClicked(item))

        vm.uiAction.test {
            vm.onEvent(EntityEvent.OnDeleteConfirmClicked)
            assertEquals(EntityUiAction.ShowSnackbar(DELETE_BLOCKED_RES, listOf(3)), awaitItem())
        }
    }

    @Test
    fun `when OnDeleteConfirmClicked completes should not emit an action`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")
        coEvery { operations.delete(item) } returns Result.success(DeletionStatus.Deleted)

        vm.onEvent(EntityEvent.OnDeleteClicked(item))
        vm.onEvent(EntityEvent.OnDeleteConfirmClicked)

        coVerify(exactly = 1) { operations.delete(item) }
    }

    @Test
    fun `when the operations fail unexpectedly should emit ShowError with the exception message`() = runTest {
        val vm = viewModel()
        val item = FakeItem(1L, "Work")
        coEvery { operations.delete(item) } returns Result.failure(IllegalStateException("boom"))

        vm.onEvent(EntityEvent.OnDeleteClicked(item))

        vm.uiAction.test {
            vm.onEvent(EntityEvent.OnDeleteConfirmClicked)
            assertEquals(EntityUiAction.ShowError("boom"), awaitItem())
        }
    }

    @Test
    fun `when OnDialogDismissed should hide the dialog`() = runTest {
        val vm = viewModel()

        vm.onEvent(EntityEvent.OnAddClicked)
        vm.onEvent(EntityEvent.OnDialogDismissed)

        assertEquals(EntityDialogState.None, vm.dialogState.value)
    }
}
