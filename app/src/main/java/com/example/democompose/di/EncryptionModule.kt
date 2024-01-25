package com.example.democompose.di

import com.example.democompose.utils.EncryptedData
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun provideEncryptedData(@ApplicationContext context: Context): EncryptedData = EncryptedData(context)
}