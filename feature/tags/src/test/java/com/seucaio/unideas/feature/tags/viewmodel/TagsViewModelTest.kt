package com.seucaio.unideas.feature.tags.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.core.common.crud.EntityCrudUiState
import com.seucaio.unideas.domain.stub.TagStub
import com.seucaio.unideas.domain.usecase.tag.AddTagUseCase
import com.seucaio.unideas.domain.usecase.tag.DeleteTagUseCase
import com.seucaio.unideas.domain.usecase.tag.GetTagsUseCase
import com.seucaio.unideas.domain.usecase.tag.RenameTagUseCase
import io.mockk.MockKAnnotations
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

/**
 * Smoke test for the wiring only — the actual add/rename/delete/dialog state machine is
 * covered once, generically, by `EntityCrudViewModelTest` (:core:common).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagsViewModelTest {

    @MockK
    private lateinit var getTags: GetTagsUseCase

    @MockK
    private lateinit var addTag: AddTagUseCase

    @MockK
    private lateinit var renameTag: RenameTagUseCase

    @MockK
    private lateinit var deleteTag: DeleteTagUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState reflects tags from GetTagsUseCase`() = runTest {
        val tags = TagStub.tags()
        every { getTags() } returns flowOf(tags)
        val vm = TagsViewModel(getTags, addTag, renameTag, deleteTag)

        vm.uiState.test {
            assertEquals(EntityCrudUiState.Success(tags), awaitItem())
        }
    }
}
