package ru.training.pikabu.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.training.pikabu.data.dao.PostDao
import ru.training.pikabu.data.model.Post

@Database(entities = [Post::class], version = 1)
abstract class AppDatabase: RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pikabu_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}