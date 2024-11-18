package ru.training.pikabu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import ru.training.pikabu.data.dao.LinkItemDao
import ru.training.pikabu.data.dao.PostDao
import ru.training.pikabu.data.model.LinkItem
import ru.training.pikabu.data.model.LinkType
import ru.training.pikabu.data.model.Post

@Database(entities = [Post::class, LinkItem::class], version = 2)
@TypeConverters(LinkTypeConverter::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun linkItemDao(): LinkItemDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pikabu_database"
                )
                    .fallbackToDestructiveMigration() // Знаю, что это приведёт к пересозданию БД и удалению данных у пользователя. Чтобы такого не происходило, необходимо реализовать Migration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LinkTypeConverter {
    @TypeConverter
    fun fromLinkType(value: LinkType) = when(value) {
        is LinkType.Internal -> LinkType.Internal.toString()
        is LinkType.External -> LinkType.External.toString()
        is LinkType.Custom -> LinkType.Custom.toString()
    }

    @TypeConverter
    fun toLinkType(value: String) = when(value) {
        LinkType.Internal.toString() -> LinkType.External
        LinkType.External.toString() -> LinkType.Internal
        LinkType.Custom.toString() -> LinkType.Custom
        else -> throw IllegalArgumentException("Unknown LinkType: $value")
    }
}