package com.seucaio.unideas.core.backup

import androidx.annotation.StringRes
import com.seucaio.unideas.core.backup.viewmodel.backup.BackupListEntry
import java.time.LocalDate

internal enum class BackupDateGroup {
    THIS_WEEK,
    LAST_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    OLDER,
}

@StringRes
internal fun BackupDateGroup.labelRes(): Int = when (this) {
    BackupDateGroup.THIS_WEEK -> R.string.backup_date_group_this_week
    BackupDateGroup.LAST_WEEK -> R.string.backup_date_group_last_week
    BackupDateGroup.THIS_MONTH -> R.string.backup_date_group_this_month
    BackupDateGroup.LAST_MONTH -> R.string.backup_date_group_last_month
    BackupDateGroup.OLDER -> R.string.backup_date_group_older
}

/**
 * No TODAY/YESTERDAY groups — each row already shows its own "Today, HH:mm"/"Yesterday, HH:mm"
 * label, so today/yesterday just fall under THIS_WEEK here. Weeks are Sunday-start, computed by
 * plain date arithmetic (not [java.time.temporal.WeekFields]'s week-of-year, which breaks around
 * year boundaries) — a date only counts as THIS_WEEK if it's on/after the most recent Sunday.
 */
internal fun LocalDate.toBackupDateGroup(today: LocalDate = LocalDate.now()): BackupDateGroup {
    val startOfThisWeek = today.startOfWeek()
    val startOfLastWeek = startOfThisWeek.minusDays(DAYS_PER_WEEK)
    return when {
        !isBefore(startOfThisWeek) -> BackupDateGroup.THIS_WEEK
        !isBefore(startOfLastWeek) -> BackupDateGroup.LAST_WEEK
        year == today.year && month == today.month -> BackupDateGroup.THIS_MONTH
        year == today.minusMonths(1).year && month == today.minusMonths(1).month -> BackupDateGroup.LAST_MONTH
        else -> BackupDateGroup.OLDER
    }
}

private fun LocalDate.startOfWeek(): LocalDate = minusDays((dayOfWeek.value % DAYS_PER_WEEK).toLong())

private const val DAYS_PER_WEEK = 7L

internal fun groupBackupsByDate(
    backups: List<BackupListEntry>,
    today: LocalDate = LocalDate.now(),
): List<Pair<BackupDateGroup, List<BackupListEntry>>> {
    val grouped = backups.groupBy { it.info.createdAt.toLocalDate().toBackupDateGroup(today) }
    return BackupDateGroup.entries.mapNotNull { group -> grouped[group]?.let { group to it } }
}
