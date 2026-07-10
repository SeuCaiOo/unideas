package com.seucaio.unideas.data.repository

import com.seucaio.unideas.data.local.dao.TagDao
import com.seucaio.unideas.data.mapper.toDomain
import com.seucaio.unideas.data.mapper.toEntity
import com.seucaio.unideas.domain.model.Tag
import com.seucaio.unideas.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepositoryImpl(
    private val tagDao: TagDao,
) : TagRepository {

    override fun getTags(): Flow<List<Tag>> =
        tagDao.getTags().map { rows -> rows.map { it.toDomain() } }

    override suspend fun insertTag(tag: Tag): Long =
        tagDao.insert(tag.toEntity())

    override suspend fun deleteTag(id: Long) = tagDao.deleteById(id)

    override suspend fun countLinkedItems(tagId: Long): Int =
        tagDao.countLinkedItems(tagId)
}
