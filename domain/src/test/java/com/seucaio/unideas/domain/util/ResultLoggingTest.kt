package com.seucaio.unideas.domain.util

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

class ResultLoggingTest {

    private class RecordingTree : Timber.Tree() {
        var loggedThrowables = mutableListOf<Throwable?>()

        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            loggedThrowables += t
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
        val result = resultCatching("tag") { 42 }

        assertEquals(Result.success(42), result)
        assertTrue(tree.loggedThrowables.isEmpty())
    }

    @Test
    fun `resultCatching returns failure and logs when block throws an unexpected error`() = runTest {
        val error = IllegalStateException("boom")

        val result = resultCatching<Unit>("tag") { throw error }

        assertTrue(result.isFailure)
        assertEquals(listOf(error), tree.loggedThrowables)
    }

    @Test
    fun `resultCatching returns failure but does not log a validation error`() = runTest {
        val error = IllegalArgumentException("blank name")

        val result = resultCatching<Unit>("tag") { throw error }

        assertTrue(result.isFailure)
        assertTrue(tree.loggedThrowables.isEmpty())
    }

    @Test
    fun `logOnError logs then rethrows so the flow still fails`() = runTest {
        val error = IllegalStateException("boom")
        val failingFlow = flow<Int> { throw error }

        val caught = runCatching { failingFlow.logOnError("tag").toList() }

        assertTrue(caught.isFailure)
        assertEquals(error, caught.exceptionOrNull())
        assertEquals(listOf(error), tree.loggedThrowables)
    }

    @Test
    fun `logOnError lets a downstream catch still degrade the flow`() = runTest {
        val error = IllegalStateException("boom")
        val failingFlow = flow<Int> { throw error }

        val items = failingFlow.logOnError("tag").catch { emit(-1) }.toList()

        assertEquals(listOf(-1), items)
        assertEquals(listOf(error), tree.loggedThrowables)
    }
}
