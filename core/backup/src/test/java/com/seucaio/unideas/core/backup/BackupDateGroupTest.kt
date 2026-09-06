package com.seucaio.unideas.core.backup

import com.seucaio.unideas.core.backup.domain.model.BackupInfo
import com.seucaio.unideas.core.backup.viewmodel.backup.BackupListEntry
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BackupDateGroupTest {

    // Tuesday — current week is Sun 2026-09-13..Sat 2026-09-19, previous week Sun 09-06..Sat 09-12.
    private val today = LocalDate.of(2026, 9, 15)

    // Sunday — the calendar week's first day, so "this week" only contains today itself.
    private val sunday = LocalDate.of(2026, 9, 6)

    @Test
    fun `when date is today should return THIS_WEEK`() {
        assertEquals(BackupDateGroup.THIS_WEEK, today.toBackupDateGroup(today))
    }

    @Test
    fun `when date is yesterday and still in the same calendar week should return THIS_WEEK`() {
        assertEquals(BackupDateGroup.THIS_WEEK, today.minusDays(1).toBackupDateGroup(today))
    }

    @Test
    fun `when date is the start of the current calendar week should return THIS_WEEK`() {
        assertEquals(BackupDateGroup.THIS_WEEK, LocalDate.of(2026, 9, 13).toBackupDateGroup(today))
    }

    @Test
    fun `when date is the end of the previous calendar week should return LAST_WEEK`() {
        assertEquals(BackupDateGroup.LAST_WEEK, LocalDate.of(2026, 9, 12).toBackupDateGroup(today))
    }

    @Test
    fun `when date is the start of the previous calendar week should return LAST_WEEK`() {
        assertEquals(BackupDateGroup.LAST_WEEK, LocalDate.of(2026, 9, 6).toBackupDateGroup(today))
    }

    @Test
    fun `when today is a Sunday yesterday should already be LAST_WEEK, not THIS_WEEK`() {
        // The bug reported manually, generalized: a Sunday starts a new week, so even yesterday
        // (Saturday) already belongs to last week.
        assertEquals(BackupDateGroup.LAST_WEEK, sunday.minusDays(1).toBackupDateGroup(sunday))
    }

    @Test
    fun `when date is before the previous calendar week but same month should return THIS_MONTH`() {
        assertEquals(BackupDateGroup.THIS_MONTH, LocalDate.of(2026, 9, 1).toBackupDateGroup(today))
    }

    @Test
    fun `when date is in the previous calendar month should return LAST_MONTH`() {
        assertEquals(BackupDateGroup.LAST_MONTH, LocalDate.of(2026, 8, 15).toBackupDateGroup(today))
    }

    @Test
    fun `when date is two months ago should return OLDER`() {
        assertEquals(BackupDateGroup.OLDER, today.minusMonths(2).toBackupDateGroup(today))
    }

    @Test
    fun `when date is a previous year should return OLDER`() {
        assertEquals(BackupDateGroup.OLDER, today.minusYears(1).toBackupDateGroup(today))
    }

    @Test
    fun `when grouping backups should bucket and order them by canonical group order`() {
        val thisWeekEntry = entryAt(today)
        val lastWeekEntry = entryAt(LocalDate.of(2026, 9, 12))
        val thisMonthEntry = entryAt(LocalDate.of(2026, 9, 1))
        val lastMonthEntry = entryAt(LocalDate.of(2026, 8, 15))
        val olderEntry = entryAt(today.minusMonths(2))

        val groups = groupBackupsByDate(
            backups = listOf(olderEntry, lastMonthEntry, thisMonthEntry, lastWeekEntry, thisWeekEntry),
            today = today,
        )

        assertEquals(
            listOf(
                BackupDateGroup.THIS_WEEK to listOf(thisWeekEntry),
                BackupDateGroup.LAST_WEEK to listOf(lastWeekEntry),
                BackupDateGroup.THIS_MONTH to listOf(thisMonthEntry),
                BackupDateGroup.LAST_MONTH to listOf(lastMonthEntry),
                BackupDateGroup.OLDER to listOf(olderEntry),
            ),
            groups,
        )
    }

    @Test
    fun `when grouping backups should preserve relative order within the same group`() {
        val first = entryAt(today, fileId = "file-1")
        val second = entryAt(today, fileId = "file-2")

        val groups = groupBackupsByDate(backups = listOf(first, second), today = today)

        assertEquals(listOf(BackupDateGroup.THIS_WEEK to listOf(first, second)), groups)
    }

    private fun entryAt(date: LocalDate, fileId: String = "file-$date"): BackupListEntry =
        BackupListEntry(BackupInfo(fileId, date.atStartOfDay(), 1024L))
}
