package com.example.democompose.di

import android.content.Context
import androidx.room.Room
import com.example.democompose.data.db.ArticlesDAO
import com.example.democompose.data.db.ArticlesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArticlesDatabase(@ApplicationContext context: Context): ArticlesDatabase {
        return Room.databaseBuilder(
            context,
            ArticlesDatabase::class.java,
            "articles_database"
        ).build()
    }

    @Provides
    fun provideArticlesDao(articlesDatabase: ArticlesDatabase): ArticlesDAO {
        return articlesDatabase.articlesDao()
    }
}