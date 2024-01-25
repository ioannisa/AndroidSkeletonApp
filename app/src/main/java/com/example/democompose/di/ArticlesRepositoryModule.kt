package com.example.democompose.di

import com.example.democompose.data.api.NewsAPI
import com.example.democompose.data.db.ArticlesDAO
import com.example.democompose.data.repository.ArticlesRepositoryImpl
import com.example.democompose.data.repository.ArticlesRepository
import com.example.democompose.manager.CredentialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ArticlesRepositoryModule {
    @Provides
    @Singleton
    fun provideArticlesRepositoryModule(
        newsAPI: NewsAPI,
        newsArticleDao: ArticlesDAO,
        credentialManager: CredentialManager
    ): ArticlesRepository {
        return ArticlesRepositoryImpl(newsAPI, newsArticleDao, credentialManager)
    }
}
