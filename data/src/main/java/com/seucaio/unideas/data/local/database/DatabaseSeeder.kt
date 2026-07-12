package com.seucaio.unideas.data.local.database

import com.seucaio.unideas.core.common.util.Constants
import com.seucaio.unideas.data.local.dao.ItemDao
import com.seucaio.unideas.data.local.dao.SectionDao
import com.seucaio.unideas.data.local.dao.TagDao
import com.seucaio.unideas.data.local.entity.ItemEntity
import com.seucaio.unideas.data.local.entity.SectionEntity
import com.seucaio.unideas.data.local.entity.TagEntity
import com.seucaio.unideas.domain.model.ItemType
import com.seucaio.unideas.domain.model.Recurrence
import com.seucaio.unideas.domain.model.SeedScope
import java.time.LocalDate
import java.time.LocalDateTime
import com.seucaio.unideas.core.common.extensions.toEpochMilli as dateToEpochMilli
import com.seucaio.unideas.data.mapper.toEpochMilli as dateTimeToEpochMilli

/**
 * Debug-only sample data, inserted directly via DAOs (not the domain use cases) — faster for
 * bulk inserts and gives full control over fields use cases don't expose (e.g. an arbitrary
 * [ItemEntity.completedAt]). Lives in the `local.database` package, excluded from `koverVerify`
 * (same convention as [UnideasDatabase]/DAOs) — no unit test obligation here, exercised manually.
 */
class DatabaseSeeder(
    private val itemDao: ItemDao,
    private val sectionDao: SectionDao,
    private val tagDao: TagDao,
) {

    suspend fun seed(scope: SeedScope) {
        when (scope) {
            SeedScope.EMPTY -> Unit
            SeedScope.BASIC -> seedBasic()
            SeedScope.FULL -> seedFull()
        }
    }

    private suspend fun seedBasic() {
        val workId = sectionDao.insert(SectionEntity(name = "Trabalho"))
        val urgentId = tagDao.insert(TagEntity(name = "urgente"))
        val today = LocalDate.now()

        insertItem(
            SeedItem(
                ItemType.TASK,
                "Pagar contas",
                description = "Boleto da internet e da água",
                dueDate = today.minusDays(1),
                sectionId = workId,
                tagIds = listOf(urgentId),
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Ligar pro dentista",
                description = "Marcar a limpeza semestral",
                dueDate = today.plusDays(1),
            ),
        )
        insertItem(SeedItem(ItemType.TASK, "Ler um livro"))
        insertItem(SeedItem(ItemType.NOTE, "Ideia de projeto", description = "App de anotações com sincronização"))
    }

    private suspend fun seedFull() {
        val sections = seedFullSections()
        val tags = seedFullTags()
        val today = LocalDate.now()

        seedFullPriorityTasks(today, sections, tags)
        seedFullExtraTasks(today, sections, tags)
        seedFullNotes(today, sections, tags)
    }

    private suspend fun seedFullSections() = FullSections(
        workId = sectionDao.insert(SectionEntity(name = "Trabalho")),
        homeId = sectionDao.insert(SectionEntity(name = "Casa")),
    )

    private suspend fun seedFullTags() = FullTags(
        urgentId = tagDao.insert(TagEntity(name = "urgente")),
        personalId = tagDao.insert(TagEntity(name = "pessoal")),
        ideaId = tagDao.insert(TagEntity(name = "ideias")),
    )

    // Enough overdue/due-soon tasks to exceed Constants.PRIORITY_PANEL_LIMIT and show "See all".
    private suspend fun seedFullPriorityTasks(today: LocalDate, sections: FullSections, tags: FullTags) {
        for (i in 1..Constants.PRIORITY_PANEL_LIMIT) {
            insertItem(
                SeedItem(
                    ItemType.TASK,
                    "Prioridade $i",
                    description = "Item de exemplo gerado pra testar o painel cheio",
                    dueDate = today.minusDays(i.toLong()),
                    sectionId = if (i % 2 == 0) sections.workId else sections.homeId,
                    tagIds = if (i == 1) listOf(tags.urgentId) else emptyList(),
                ),
            )
        }
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Ligar pro dentista",
                description = "Marcar a limpeza semestral",
                dueDate = today.plusDays(Constants.DUE_SOON_DAYS.toLong()),
            ),
        )
    }

    // Recurring, completed, no-date cases — visual coverage beyond the fixed panel.
    private suspend fun seedFullExtraTasks(today: LocalDate, sections: FullSections, tags: FullTags) {
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Planejar viagem",
                description = "Pesquisar passagens e hospedagem",
                dueDate = today.plusDays(FAR_FUTURE_DAYS),
                sectionId = sections.homeId,
                tagIds = listOf(tags.personalId),
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Regar as plantas",
                description = "Suculentas da sala e da varanda",
                dueDate = today.plusDays(RECURRING_DAYS),
                sectionId = sections.homeId,
                recurrence = Recurrence.Weekly,
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Renovar assinatura",
                description = "Plano anual do streaming",
                dueDate = today.minusDays(COMPLETED_DAYS_AGO),
                tagIds = listOf(tags.urgentId),
                completedAt = LocalDateTime.now(),
            ),
        )
        insertItem(SeedItem(ItemType.TASK, "Ler um livro"))
    }

    private suspend fun seedFullNotes(today: LocalDate, sections: FullSections, tags: FullTags) {
        insertItem(
            SeedItem(
                ItemType.NOTE,
                "Ideias de presente",
                description = "Aniversário da Ana em dois meses",
                sectionId = sections.homeId,
                tagIds = listOf(tags.ideaId, tags.personalId),
            ),
        )
        insertItem(SeedItem(ItemType.NOTE, "Pensamento aleatório"))
        insertItem(
            SeedItem(
                ItemType.NOTE,
                "Roteiro da viagem",
                description = "Rascunho dos lugares pra visitar",
                dueDate = today.plusDays(NOTE_DUE_DAYS),
                sectionId = sections.workId,
            ),
        )
    }

    private suspend fun insertItem(spec: SeedItem) {
        val entity = ItemEntity(
            type = spec.type,
            title = spec.title,
            description = spec.description,
            sectionId = spec.sectionId,
            dueDate = spec.dueDate?.dateToEpochMilli(),
            recurrence = spec.recurrence,
            completedAt = spec.completedAt?.dateTimeToEpochMilli(),
            createdAt = LocalDateTime.now().dateTimeToEpochMilli(),
        )
        itemDao.insertItemWithTags(entity, spec.tagIds)
    }

    private data class FullSections(val workId: Long, val homeId: Long)

    private data class FullTags(val urgentId: Long, val personalId: Long, val ideaId: Long)

    /** Bundles [ItemEntity]'s optional fields — a data class so `LongParameterList` doesn't apply. */
    private data class SeedItem(
        val type: ItemType,
        val title: String,
        val description: String? = null,
        val dueDate: LocalDate? = null,
        val sectionId: Long? = null,
        val tagIds: List<Long> = emptyList(),
        val recurrence: Recurrence = Recurrence.None,
        val completedAt: LocalDateTime? = null,
    )

    private companion object {
        const val FAR_FUTURE_DAYS = 30L
        const val RECURRING_DAYS = 2L
        const val COMPLETED_DAYS_AGO = 1L
        const val NOTE_DUE_DAYS = 10L
    }
}
