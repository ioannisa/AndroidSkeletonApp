package com.example.democompose.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ArticleDB::class], version = 1, exportSchema = false)
abstract class ArticlesDatabase : RoomDatabase() {
    abstract fun articlesDao(): ArticlesDAO
}