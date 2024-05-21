package com.example.democompose.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import eu.anifantakis.mod.coredata.persist.EncryptionManager
import eu.anifantakis.mod.coredata.persist.PersistManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun provideEncryptedPersistence(@ApplicationContext context: Context): PersistManager = PersistManager(context, "myKeyAlias")

    @Provides
    @Singleton
    fun provideEncryptedManager(): EncryptionManager = EncryptionManager("myKeyAlias")
}