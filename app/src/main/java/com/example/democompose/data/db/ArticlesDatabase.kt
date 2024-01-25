package com.example.democompose.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.democompose.data.model.Article

@Database(entities = [Article::class], version = 1, exportSchema = false)
abstract class ArticlesDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDAO
}