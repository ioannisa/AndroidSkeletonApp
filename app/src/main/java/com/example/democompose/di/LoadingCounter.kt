package com.example.democompose.di

import com.example.democompose.utils.ObservableLoadingInteger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoadingCounter {

    @Singleton
    @Provides
    fun provideLoadingCounter(): ObservableLoadingInteger {
        return ObservableLoadingInteger(0)
    }
}