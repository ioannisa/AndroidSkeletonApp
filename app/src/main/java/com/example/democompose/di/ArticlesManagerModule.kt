package com.example.democompose.di

import com.example.democompose.manager.ArticlesManager
import com.example.democompose.manager.ArticlesManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ArticlesManagerModuleBinds {

    @Binds
    @Singleton
    abstract fun bindArticlesManager(impl: ArticlesManagerImpl): ArticlesManager
}