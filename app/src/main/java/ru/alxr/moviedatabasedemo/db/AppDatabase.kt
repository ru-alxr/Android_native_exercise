package ru.alxr.moviedatabasedemo.db

import android.app.Activity
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.alxr.moviedatabasedemo.db.entity.MovieDAO
import ru.alxr.moviedatabasedemo.db.entity.MovieEntity

@Database(
    entities = [MovieEntity::class], version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase(){

    abstract fun getMovieDAO(): MovieDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (context is Activity) throw RuntimeException("Must not be activity context")
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    val instance = Room
                        .databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "voice.db"
                        )
                        .build()
                    INSTANCE = instance
                }
            }
            return INSTANCE
        }
    }

}