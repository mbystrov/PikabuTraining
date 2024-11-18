package ru.training.pikabu.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.training.pikabu.data.model.LinkItem

@Dao
interface LinkItemDao {

    @Query("SELECT * FROM links WHERE type = :type")
    suspend fun getAllLinksByType(type: String): List<LinkItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLink(linkItem: LinkItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // в обоих методах addLink(s) изменю стратегию решения конфликтов с REPLACE
    suspend fun addLinks(posts: List<LinkItem>)

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()
}