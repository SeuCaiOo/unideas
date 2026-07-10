package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.dao.SectionDao
import com.seucaio.unideas.data.mapper.toDomain
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.model.Section
import com.seucaio.unideas.domain.repository.SectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SectionRepositoryImpl(
    private val sectionDao: SectionDao,
) : SectionRepository {

    override fun getSections(): Flow<List<Section>> =
        sectionDao.getSections().map { rows -> rows.map { it.toDomain() } }

    override fun getSectionsByTags(tagIds: List<Long>): Flow<List<Section>> =
        sectionDao.getSectionsByTags(tagIds).map { rows -> rows.map { it.toDomain() } }

    override suspend fun insertSection(section: Section): Long =
        sectionDao.insert(section.toEntity())

    override suspend fun updateSection(section: Section) =
        sectionDao.update(section.toEntity())

    override suspend fun deleteSection(id: Long) = sectionDao.deleteById(id)

    override suspend fun countLinkedItems(sectionId: Long): Int =
        sectionDao.countLinkedItems(sectionId)
}
