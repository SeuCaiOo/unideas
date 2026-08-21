package com.seucaio.unideas.data.local.database

import com.seucaio.unideas.core.common.extensions.toEpochMilli
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
        insertItem(SeedItem(ItemType.TASK, "Salve", description = MARKDOWN_EXAMPLE_DESCRIPTION))
    }

    private suspend fun seedFull() {
        val sections = FullSections(
            workId = sectionDao.insert(SectionEntity(name = "Trabalho")),
            homeId = sectionDao.insert(SectionEntity(name = "Casa")),
        )
        val tags = FullTags(
            urgentId = tagDao.insert(TagEntity(name = "urgente")),
            personalId = tagDao.insert(TagEntity(name = "pessoal")),
            ideaId = tagDao.insert(TagEntity(name = "ideias")),
        )
        val today = LocalDate.now()

        seedFullPriorityTasks(today, sections, tags)
        seedFullExtraTasks(today, sections, tags)
        seedFullReevaluationScenarios(today, sections)
        seedFullNotes(today, sections, tags)
        seedFullDescriptionScenarios(today, sections)
    }

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
        seedFullRecurringTasks(today, sections)
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

    // One example per Recurrence type — visual coverage for #130's per-type label formatting.
    private suspend fun seedFullRecurringTasks(today: LocalDate, sections: FullSections) {
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
                "Pagar aluguel",
                description = "Boleto do apartamento",
                dueDate = today.withDayOfMonth(minOf(RENT_DAY_OF_MONTH, today.lengthOfMonth())),
                sectionId = sections.homeId,
                recurrence = Recurrence.Monthly,
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Trocar filtro de água",
                description = "Filtro do purificador da cozinha",
                dueDate = today.plusDays(EVERY_N_DAYS_EXAMPLE),
                recurrence = Recurrence.EveryNDays(EVERY_N_DAYS_EXAMPLE.toInt()),
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Tomar remédio",
                dueDate = today,
                recurrence = Recurrence.Daily,
            ),
        )
    }

    // Pre-set item/history states the occurrence reevaluation engine (#151) reads on its own —
    // lets a manual pull-to-refresh test exercise the dedup/carry-over paths without editing
    // the DB by hand. Titles double as the expected outcome, checkable at a glance post-refresh.
    private suspend fun seedFullReevaluationScenarios(today: LocalDate, sections: FullSections) {
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Reavaliação: já concluída hoje (não deve notificar)",
                description = "dueDate = hoje, lastCompletedScheduledDate = hoje",
                dueDate = today,
                sectionId = sections.homeId,
                recurrence = Recurrence.Daily,
                lastCompletedScheduledDate = today,
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Reavaliação: concluída, aguardando avanço (sem MISSED duplicado)",
                description = "dueDate = ontem, lastCompletedScheduledDate = ontem",
                dueDate = today.minusDays(1),
                sectionId = sections.homeId,
                recurrence = Recurrence.Daily,
                lastCompletedScheduledDate = today.minusDays(1),
            ),
        )
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Reavaliação: extensão pendente nunca resolvida",
                description = "dueDate atrasado, já adiado uma vez, nunca resolvido",
                dueDate = today.minusWeeks(1),
                sectionId = sections.homeId,
                recurrence = Recurrence.Weekly,
                pendingExtensionOriginalDueDate = today.minusWeeks(2),
                pendingExtensionCount = 1,
            ),
        )
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

    // Long/Markdown descriptions — visual coverage for #165's list-item description preview
    // (1-line collapsed / 5-line expanded chevron, and markdown source shown as plain text).
    private suspend fun seedFullDescriptionScenarios(today: LocalDate, sections: FullSections) {
        insertItem(
            SeedItem(
                ItemType.TASK,
                "Planejar mudança de apartamento",
                description = LONG_EXAMPLE_DESCRIPTION,
                dueDate = today.minusDays(LONG_DESCRIPTION_OVERDUE_DAYS),
                sectionId = sections.homeId,
            ),
        )
        insertItem(
            SeedItem(
                ItemType.NOTE,
                "Notas da reunião",
                description = MARKDOWN_EXAMPLE_DESCRIPTION,
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
            dueDate = spec.dueDate?.toEpochMilli(),
            recurrence = spec.recurrence,
            completedAt = spec.completedAt?.toEpochMilli(),
            createdAt = LocalDateTime.now().toEpochMilli(),
            lastCompletedScheduledDate = spec.lastCompletedScheduledDate?.toEpochMilli(),
            pendingExtensionOriginalDueDate = spec.pendingExtensionOriginalDueDate?.toEpochMilli(),
            pendingExtensionCount = spec.pendingExtensionCount,
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
        val lastCompletedScheduledDate: LocalDate? = null,
        val pendingExtensionOriginalDueDate: LocalDate? = null,
        val pendingExtensionCount: Int = 0,
    )

    private companion object {
        const val FAR_FUTURE_DAYS = 30L
        const val RECURRING_DAYS = 2L
        const val COMPLETED_DAYS_AGO = 1L
        const val NOTE_DUE_DAYS = 10L
        const val RENT_DAY_OF_MONTH = 5
        const val EVERY_N_DAYS_EXAMPLE = 15L
        const val LONG_DESCRIPTION_OVERDUE_DAYS = 3L

        /**
         * Overflows past 5 lines even expanded — exercises the collapsed/expanded chevron in
         * `ListItemRow`. Also mixes in bold/italic markdown syntax to check how the list-item
         * description preview handles it (rendered vs. shown as raw `**`/`_` characters).
         */
        val LONG_EXAMPLE_DESCRIPTION = """
            Confirmar com a operadora se o pacote de dados internacional **já foi ativado** antes
            de embarcar, porque da última vez a confirmação chegou só depois do voo e ficamos sem
            internet por dois dias inteiros na chegada, o que atrapalhou bastante pra achar o hotel
            e pedir um carro. Também vale revisar o _seguro viagem_ e imprimir os vouchers, porque
            o wi-fi do aeroporto de conexão costuma ser instável.
        """.trimIndent().replace("\n", " ")

        /** Exercises every Markdown syntax the toolbar inserts (`MarkdownSyntaxInserter`), for manual visual checks. */
        val MARKDOWN_EXAMPLE_DESCRIPTION = """
            Texto normal, **negrito**, _italico_ e ~~riscado~~.

            - Item da lista
            - Outro item

            1. Primeiro item numerado
            2. Segundo item numerado

            - [ ] Tarefa pendente
            - [x] Tarefa concluida
        """.trimIndent()
    }
}
