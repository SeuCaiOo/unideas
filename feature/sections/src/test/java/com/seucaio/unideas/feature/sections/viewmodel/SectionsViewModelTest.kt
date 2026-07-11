package com.seucaio.unideas.feature.sections.viewmodel

import app.cash.turbine.test
import com.seucaio.unideas.core.common.crud.EntityCrudUiState
import com.seucaio.unideas.domain.stub.SectionStub
import com.seucaio.unideas.domain.usecase.section.AddSectionUseCase
import com.seucaio.unideas.domain.usecase.section.DeleteSectionUseCase
import com.seucaio.unideas.domain.usecase.section.GetSectionsUseCase
import com.seucaio.unideas.domain.usecase.section.RenameSectionUseCase
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
class SectionsViewModelTest {

    @MockK
    private lateinit var getSections: GetSectionsUseCase

    @MockK
    private lateinit var addSection: AddSectionUseCase

    @MockK
    private lateinit var renameSection: RenameSectionUseCase

    @MockK
    private lateinit var deleteSection: DeleteSectionUseCase

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
    fun `uiState reflects sections from GetSectionsUseCase`() = runTest {
        val sections = SectionStub.sections()
        every { getSections() } returns flowOf(sections)
        val vm = SectionsViewModel(getSections, addSection, renameSection, deleteSection)

        vm.uiState.test {
            assertEquals(EntityCrudUiState.Success(sections), awaitItem())
        }
    }
}
