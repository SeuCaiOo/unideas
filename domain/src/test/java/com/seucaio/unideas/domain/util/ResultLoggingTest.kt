package com.seucaio.unideas.domain.util

import com.seucaio.unideas.domain.usecase.UseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class ResultLoggingTest : UseCase {

    private class RecordingTree : Timber.Tree() {
        var loggedThrowables = mutableListOf<Throwable?>()
        var loggedTags = mutableListOf<String?>()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            loggedThrowables += t
            loggedTags += tag
        }
    }

    private val tree = RecordingTree()

    @Before
    fun setUp() {
        Timber.plant(tree)
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun `resultCatching returns success and logs nothing when block succeeds`() = runTest {
        val result = resultCatching { 42 }

        assertEquals(Result.success(42), result)
        assertTrue(tree.loggedThrowables.isEmpty())
    }

    @Test
    fun `resultCatching returns failure and logs when block throws an unexpected error`() = runTest {
        val error = IllegalStateException("boom")

        val result = resultCatching<Unit> { throw error }

        assertTrue(result.isFailure)
        assertEquals(listOf(error), tree.loggedThrowables)
    }

    @Test
    fun `resultCatching tags the log with the caller's own class, not a hardcoded string`() = runTest {
        val error = IllegalStateException("boom")

        FakeUseCase().invoke(error)

        assertEquals(listOf("FakeUseCase"), tree.loggedTags)
    }

    @Test
    fun `resultCatching returns failure but does not log a validation error`() = runTest {
        val error = IllegalArgumentException("blank name")

        val result = resultCatching<Unit> { throw error }

        assertTrue(result.isFailure)
        assertTrue(tree.loggedThrowables.isEmpty())
    }

    @Test
    fun `logOnError logs then rethrows so the flow still fails`() = runTest {
        val error = IllegalStateException("boom")
        val failingFlow = flow<Int> { throw error }

        val caught = runCatching { failingFlow.logOnError(this@ResultLoggingTest).toList() }

        assertTrue(caught.isFailure)
        assertEquals(error, caught.exceptionOrNull())
        assertEquals(listOf(error), tree.loggedThrowables)
    }

    @Test
    fun `logOnError lets a downstream catch still degrade the flow`() = runTest {
        val error = IllegalStateException("boom")
        val failingFlow = flow<Int> { throw error }

        val items = failingFlow.logOnError(this@ResultLoggingTest).catch { emit(-1) }.toList()

        assertEquals(listOf(-1), items)
        assertEquals(listOf(error), tree.loggedThrowables)
    }

    @Test
    fun `logOnError tags the log with owner's own class, not a hardcoded string`() = runTest {
        val error = IllegalStateException("boom")
        val failingFlow = flow<Int> { throw error }

        runCatching { failingFlow.logOnError(FakeUseCase()).toList() }

        assertEquals(listOf("FakeUseCase"), tree.loggedTags)
    }

    private class FakeUseCase : UseCase {
        suspend operator fun invoke(error: Throwable): Result<Unit> = resultCatching { throw error }
    }
}
