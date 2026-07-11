package com.seucaio.unideas.feature.items.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seucaio.unideas.core.common.extensions.toFormattedDateString
import com.seucaio.unideas.domain.model.Item
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.usecase.item.CompleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.DeleteItemUseCase
import com.seucaio.unideas.domain.usecase.item.GetItemDetailUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.feature.items.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * ViewModel for the read-only item detail screen. Unlike the form (#54), there's no in-progress
 * editing to protect — `uiState` stays fully reactive to `GetItemDetailUseCase`, same "no
 * combine" pattern as Sections/Tags. [GetSectionsUseCase] is combined in only to resolve
 * `sectionId` into a display name — same problem `ItemFormViewModel` already solved.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailViewModel(
    private val itemId: Long,
    private val getItemDetail: GetItemDetailUseCase,
    private val getSections: GetSectionsUseCase,
    private val deleteItem: DeleteItemUseCase,
    private val completeItem: CompleteItemUseCase,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    val uiState: StateFlow<ItemDetailUiState> = retryTrigger.flatMapLatest {
        combine(getItemDetail(itemId), getSections()) { item, sections ->
            (
                item?.let { ItemDetailUiState.Success(it, sections.firstOrNull { s -> s.id == it.sectionId }?.name) }
                    ?: ItemDetailUiState.Error(R.string.item_detail_load_error)
                ) as ItemDetailUiState
        }
            .onStart { emit(ItemDetailUiState.Loading) }
            .catch { emit(ItemDetailUiState.Error(R.string.item_detail_load_error)) }
    }.stateIn(viewModelScope, WhileSubscribed(5_000), ItemDetailUiState.Loading)

    // Mirrors the latest Success item without casting uiState.value (forbidden by mvi.md) —
    // handleComplete/handleShare need the domain object, not just the sealed UI state.
    private var currentItem: Item? = null

    init {
        uiState.onEach { state ->
            if (state is ItemDetailUiState.Success) currentItem = state.item
        }.launchIn(viewModelScope)
    }

    private val _dialogState = MutableStateFlow<ItemDetailDialogState>(ItemDetailDialogState.None)
    val dialogState: StateFlow<ItemDetailDialogState> = _dialogState.asStateFlow()

    private val _uiAction = Channel<ItemDetailUiAction>(Channel.BUFFERED)
    val uiAction: Flow<ItemDetailUiAction> = _uiAction.receiveAsFlow()

    fun onEvent(event: ItemDetailEvent) {
        when (event) {
            is ItemDetailEvent.OnDeleteClicked -> _dialogState.update { ItemDetailDialogState.DeleteConfirm }
            is ItemDetailEvent.OnDialogDismissed -> _dialogState.update { ItemDetailDialogState.None }
            is ItemDetailEvent.OnDeleteConfirmClicked -> handleDelete()
            is ItemDetailEvent.OnEditClicked -> sendUiAction(ItemDetailUiAction.NavigateToEdit(itemId))
            is ItemDetailEvent.OnCompleteClicked -> handleComplete()
            is ItemDetailEvent.OnShareClicked -> handleShare()
            is ItemDetailEvent.OnRetryClicked -> retryTrigger.tryEmit(Unit)
        }
    }

    private fun handleDelete() = viewModelScope.launch {
        _dialogState.update { ItemDetailDialogState.None }
        runCatching { deleteItem(itemId) }
            .onSuccess { sendUiAction(ItemDetailUiAction.NavigateBack) }
            .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleComplete() = viewModelScope.launch {
        val item = currentItem ?: return@launch
        if (item.type != ItemType.TASK) return@launch
        completeItem(item, LocalDateTime.now())
            .onFailure { sendUiAction(ItemDetailUiAction.ShowError(it.message.orEmpty())) }
    }

    private fun handleShare() {
        val item = currentItem ?: return
        sendUiAction(ItemDetailUiAction.ShareText(buildShareText(item)))
    }

    private fun buildShareText(item: Item): String = buildString {
        appendLine(item.title)
        item.description?.let { appendLine(it) }
        item.dueDate?.let { appendLine(it.toFormattedDateString()) }
    }

    private fun sendUiAction(action: ItemDetailUiAction) = viewModelScope.launch { _uiAction.send(action) }
}
