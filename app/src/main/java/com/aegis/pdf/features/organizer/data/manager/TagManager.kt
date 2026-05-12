package com.aegis.pdf.features.organizer.data.manager

import android.graphics.Color
import com.aegis.pdf.features.organizer.data.local.dao.TagDao
import com.aegis.pdf.features.organizer.data.local.entity.FileTagCrossRef
import com.aegis.pdf.features.organizer.data.local.entity.RecentFileEntity
import com.aegis.pdf.features.organizer.data.local.entity.TagEntity
import com.aegis.pdf.features.organizer.domain.result.TagResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagManager @Inject constructor(
    private val tagDao: TagDao
) {

    private val defaultTags = listOf(
        TagEntity("1", "Important", Color.RED),
        TagEntity("2", "Work", Color.BLUE),
        TagEntity("3", "Personal", Color.GREEN),
        TagEntity("4", "Urgent", Color.rgb(255, 152, 0)),
        TagEntity("5", "Archive", Color.GRAY)
    )

    suspend fun initDefaultTags() {
        withContext(Dispatchers.IO) {
            try {
                val count = tagDao.getTagCount()
                if (count == 0) {
                    defaultTags.forEach { tagDao.insertTag(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun createTag(name: String, color: Int): TagResult<TagEntity> {
        return withContext(Dispatchers.IO) {
            try {
                if (name.isBlank()) {
                    return@withContext TagResult.Error("Tag name cannot be empty")
                }

                val tag = TagEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name.trim(),
                    color = color
                )
                tagDao.insertTag(tag)
                TagResult.Success(tag)
            } catch (e: Exception) {
                TagResult.Error("Failed to create tag", e)
            }
        }
    }

    suspend fun addTagToFile(fileId: String, tagId: String): TagResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                tagDao.addFileTag(FileTagCrossRef(fileId, tagId))
                TagResult.Success(Unit)
            } catch (e: Exception) {
                TagResult.Error("Failed to add tag to file", e)
            }
        }
    }

    suspend fun removeTagFromFile(fileId: String, tagId: String): TagResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                tagDao.removeFileTag(FileTagCrossRef(fileId, tagId))
                TagResult.Success(Unit)
            } catch (e: Exception) {
                TagResult.Error("Failed to remove tag from file", e)
            }
        }
    }

    suspend fun getTagsForFile(fileId: String): TagResult<List<TagEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val tags = tagDao.getTagsForFile(fileId)
                TagResult.Success(tags)
            } catch (e: Exception) {
                TagResult.Error("Failed to load tags", e)
            }
        }
    }

    suspend fun getFilesWithTag(tagId: String): TagResult<List<RecentFileEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val files = tagDao.getFilesForTag(tagId)
                TagResult.Success(files)
            } catch (e: Exception) {
                TagResult.Error("Failed to load files", e)
            }
        }
    }

    suspend fun getAllTags(): TagResult<List<TagEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val tags = tagDao.getAllTags()
                TagResult.Success(tags)
            } catch (e: Exception) {
                TagResult.Error("Failed to load all tags", e)
            }
        }
    }

    suspend fun deleteTag(tag: TagEntity): TagResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                tagDao.deleteTag(tag)
                TagResult.Success(Unit)
            } catch (e: Exception) {
                TagResult.Error("Failed to delete tag", e)
            }
        }
    }

    suspend fun updateTagColor(tagId: String, color: Int): TagResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val allTags = tagDao.getAllTags()
                val tag = allTags.find { it.id == tagId }
                    ?: return@withContext TagResult.Error("Tag not found")

                tagDao.insertTag(tag.copy(color = color))
                TagResult.Success(Unit)
            } catch (e: Exception) {
                TagResult.Error("Failed to update tag color", e)
            }
        }
    }
}