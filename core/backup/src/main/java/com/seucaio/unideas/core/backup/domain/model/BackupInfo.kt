package com.seucaio.unideas.core.backup.domain.model

import java.time.LocalDateTime

data class BackupInfo(
    val fileId: String,
    val createdAt: LocalDateTime,
    val sizeBytes: Long,
)
