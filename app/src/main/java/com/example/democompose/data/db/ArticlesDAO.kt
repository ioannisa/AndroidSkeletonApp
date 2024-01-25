package com.example.democompose.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.democompose.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticlesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveArticles(articles: List<Article>)

    @Query(value = "DELETE FROM article")
    suspend fun deleteAllArticles()

    @Query("DELETE FROM article WHERE url NOT IN (:articleIds)")
    suspend fun deleteArticlesNotIn(articleIds: List<String>)

    @Query(value = "SELECT * FROM article")
    fun getArticles(): Flow<List<Article>>
}